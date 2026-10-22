package com.jorge.cervelog.ui.mapa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.clustering.rememberClusterManager
import com.google.maps.android.compose.rememberCameraPositionState
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.ui.components.BUENOS_AIRES
import com.jorge.cervelog.ui.components.BarraSuperior
import com.jorge.cervelog.ui.components.MensajeSnackbar
import com.jorge.cervelog.ui.components.RatingStars
import com.jorge.cervelog.ui.components.TarjetaDatos
import com.jorge.cervelog.ui.components.ZOOM_BARRIO
import com.jorge.cervelog.ui.components.ZOOM_CIUDAD
import com.jorge.cervelog.ui.components.formatearAbv
import com.jorge.cervelog.ui.components.formatearFecha
import com.jorge.cervelog.ui.components.rememberPedirUbicacion
import com.jorge.cervelog.ui.components.subtitulo
import com.jorge.cervelog.ui.components.tienePermisoUbicacion
import com.jorge.cervelog.ui.components.toLatLng
import com.jorge.cervelog.ui.components.toZona
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme
import java.time.Instant

// Adaptador para el clustering
private class MarcadorPublicacion(val publicacion: Publicacion, autor: String) : ClusterItem {
    override val position = publicacion.ubicacion.toLatLng()
    override val title = publicacion.nombre
    override val snippet = "${"★".repeat(publicacion.puntuacion)} · $autor"
    override val zIndex: Float? = null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaComunitarioRoute(
    onVerMiCerveza: (String) -> Unit,
    viewModel: MapaComunitarioViewModel = viewModel(factory = MapaComunitarioViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    MensajeSnackbar(state.error, snackbarHostState, viewModel::onErrorMostrado)

    // Solo el id, para que sobreviva al giro de pantalla
    var seleccionadaId by rememberSaveable { mutableStateOf<String?>(null) }
    val seleccionada = state.publicaciones.find { it.id == seleccionadaId }

    // El punto azul de mi ubicación necesita permiso
    var permisoUbicacion by remember { mutableStateOf(tienePermisoUbicacion(context)) }
    val pedirUbicacion = rememberPedirUbicacion { permisoUbicacion = it }
    LaunchedEffect(permisoUbicacion) {
        if (permisoUbicacion) viewModel.onCentrarEnMiUbicacion() else pedirUbicacion()
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(BUENOS_AIRES, ZOOM_CIUDAD)
    }
    val miUbicacion by viewModel.miUbicacion.collectAsStateWithLifecycle()
    LaunchedEffect(miUbicacion) {
        val posicion = miUbicacion ?: return@LaunchedEffect
        // Sin animación para no disparar varias recargas
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(posicion.toLatLng(), ZOOM_BARRIO))
    }
    val cargarZonaVisible = {
        cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { viewModel.onZonaVisible(it.toZona()) }
    }
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) cargarZonaVisible()
    }

    val tu = stringResource(R.string.tu)
    val marcadores = remember(state.publicaciones) {
        state.publicaciones.map {
            MarcadorPublicacion(it, autor = if (it.autorUid == viewModel.uidUsuario) tu else it.autorEmail)
        }
    }

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.mapa_cervecero)) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(isMyLocationEnabled = permisoUbicacion),
                onMapLoaded = { cargarZonaVisible() },
            ) {
                // Sin animación porque los marcadores tiemblan al redibujarse
                val clusterManager = rememberClusterManager<MarcadorPublicacion>()
                if (clusterManager != null) {
                    SideEffect {
                        clusterManager.setAnimation(false)
                        // true sustituye el bocadillo de Google por la hoja de detalle
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

// Solo datos públicos: la foto y las notas son privadas
@Composable
fun DetallePublicacion(publicacion: Publicacion, esMia: Boolean, onVerMiCerveza: () -> Unit) {
    val sinDato = stringResource(R.string.sin_dato)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppTheme.spacing.xl, end = AppTheme.spacing.xl, bottom = AppTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        Text(publicacion.nombre, style = AppTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        val subtitulo = subtitulo(publicacion.cerveceria, publicacion.estilo)
        if (subtitulo.isNotEmpty()) {
            Text(subtitulo, style = AppTheme.typography.titleMedium, color = AppTheme.colors.onSurfaceVariant)
        }
        RatingStars(publicacion.puntuacion, size = 24.dp)

        TarjetaDatos(
            stringResource(R.string.abv) to (publicacion.abv?.formatearAbv() ?: sinDato),
            stringResource(R.string.ibu) to (publicacion.ibu?.toString() ?: sinDato),
            stringResource(R.string.lugar) to publicacion.lugar.ifBlank { sinDato },
            stringResource(R.string.fecha) to (publicacion.fecha?.formatearFecha() ?: sinDato),
            stringResource(R.string.registrada_por) to if (esMia) stringResource(R.string.ti) else publicacion.autorEmail,
        )

        if (esMia) {
            Button(onClick = onVerMiCerveza, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.ver_mi_cerveza))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun DetallePublicacionPreview() {
    CerveLogTheme {
        DetallePublicacion(
            Publicacion(
                nombre = "Antares Kölsch",
                cerveceria = "Antares",
                estilo = "Kölsch",
                abv = 5.0,
                puntuacion = 4,
                lugar = "Recoleta",
                autorEmail = "ana@aplicaciones.com",
                fecha = Instant.now(),
            ),
            esMia = false,
            onVerMiCerveza = {},
        )
    }
}
