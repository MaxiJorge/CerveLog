package com.jorge.cervelog.ui.mapa

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.clustering.rememberClusterManager
import com.google.maps.android.compose.rememberCameraPositionState
import com.jorge.cervelog.data.model.Publicacion
import com.jorge.cervelog.ui.components.RatingStars
import com.jorge.cervelog.ui.components.formatearAbv
import com.jorge.cervelog.ui.components.formatearFecha

// Si no hay permiso de ubicación, el mapa empieza en Buenos Aires
private val POSICION_INICIAL = LatLng(-34.6037, -58.3816)
private const val ZOOM_CIUDAD = 11f
private const val ZOOM_BARRIO = 13f

// Adaptador para que el clustering sepa dónde va cada publicación y qué mostrar al pulsarla
private class MarcadorPublicacion(val publicacion: Publicacion, esMia: Boolean) : ClusterItem {
    private val autor = if (esMia) "Tú" else publicacion.autorEmail
    override val position = LatLng(publicacion.latitud, publicacion.longitud)
    override val title = publicacion.nombre
    override val snippet = "${"★".repeat(publicacion.puntuacion)} · $autor"
    override val zIndex: Float? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaComunitarioScreen(
    onVerMiCerveza: (String) -> Unit,
    viewModel: MapaComunitarioViewModel = viewModel(factory = MapaComunitarioViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    // Se guarda solo el id para que sobreviva a un giro de pantalla
    var seleccionadaId by rememberSaveable { mutableStateOf<String?>(null) }
    val seleccionada = state.publicaciones.find { it.id == seleccionadaId }

    // El punto azul de «mi ubicación» solo se puede activar con permiso
    var permisoUbicacion by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED,
        )
    }
    val permisoLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
    ) { permisos -> permisoUbicacion = permisos.values.any { it } }
    LaunchedEffect(permisoUbicacion) {
        if (permisoUbicacion) {
            viewModel.centrarEnMiUbicacion()
        } else {
            permisoLauncher.launch(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            )
        }
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(POSICION_INICIAL, ZOOM_CIUDAD)
    }
    val miUbicacion by viewModel.miUbicacion.collectAsStateWithLifecycle()
    LaunchedEffect(miUbicacion) {
        val posicion = miUbicacion ?: return@LaunchedEffect
        // Sin animación: un único salto en vez de un zoom que dispara varias recargas
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(posicion, ZOOM_BARRIO))
    }
    val cargarZonaVisible = {
        cameraPositionState.projection?.visibleRegion?.latLngBounds?.let(viewModel::cargarZona)
    }
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) cargarZonaVisible()
    }

    LaunchedEffect(state.error) {
        val texto = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(texto)
        viewModel.errorMostrado()
    }

    val marcadores = remember(state.publicaciones) {
        state.publicaciones.map { MarcadorPublicacion(it, esMia = it.autorUid == viewModel.uidUsuario) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Mapa cervecero", fontWeight = FontWeight.Bold) },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
                ),
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(Modifier.padding(padding).fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = permisoUbicacion),
                onMapLoaded = { cargarZonaVisible() },
            ) {
                // Agrupa los marcadores cercanos en uno con el número de cervezas.
                // Sin animación, porque al redibujar los marcadores se ven temblar.
                val clusterManager = rememberClusterManager<MarcadorPublicacion>()
                if (clusterManager != null) {
                    SideEffect {
                        clusterManager.setAnimation(false)
                        // true: en vez del bocadillo de Google se abre la hoja con los detalles
                        clusterManager.setOnClusterItemClickListener {
                            seleccionadaId = it.publicacion.id
                            true
                        }
                    }
                    Clustering(items = marcadores, clusterManager = clusterManager)
                }
            }
            if (state.cargando) {
                LinearProgressIndicator(Modifier.fillMaxWidth())
            }
        }
    }

    if (seleccionada != null) {
        ModalBottomSheet(onDismissRequest = { seleccionadaId = null }) {
            DetallePublicacion(
                publicacion = seleccionada,
                esMia = seleccionada.autorUid == viewModel.uidUsuario,
                onVerMiCerveza = {
                    seleccionadaId = null
                    onVerMiCerveza(seleccionada.id)
                },
            )
        }
    }
}

// Solo los datos públicos: la foto y las notas se quedan en la cerveza privada del autor
@Composable
private fun DetallePublicacion(publicacion: Publicacion, esMia: Boolean, onVerMiCerveza: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(publicacion.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        val subtitulo = listOf(publicacion.cerveceria, publicacion.estilo).filter { it.isNotBlank() }.joinToString(" · ")
        if (subtitulo.isNotEmpty()) {
            Text(subtitulo, style = MaterialTheme.typography.titleMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        RatingStars(publicacion.puntuacion, size = 24.dp)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceContainer)) {
            Column(Modifier.padding(16.dp)) {
                Dato("ABV", publicacion.abv?.formatearAbv() ?: "—")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Dato("IBU", publicacion.ibu?.toString() ?: "—")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Dato("Lugar", publicacion.lugar.ifBlank { "—" })
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Dato("Fecha", publicacion.fecha?.formatearFecha() ?: "—")
                HorizontalDivider(Modifier.padding(vertical = 8.dp))
                Dato("Registrada por", if (esMia) "Ti" else publicacion.autorEmail)
            }
        }

        if (esMia) {
            Button(onClick = onVerMiCerveza, modifier = Modifier.fillMaxWidth()) {
                Text("Ver mi cerveza")
            }
        }
    }
}

@Composable
private fun Dato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(
            valor,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = 16.dp),
        )
    }
}
