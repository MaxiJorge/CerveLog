package com.jorge.cervelog.ui.mapa

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
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.clustering.rememberClusterManager
import com.google.maps.android.compose.clustering.rememberClusterRenderer
import com.google.maps.android.compose.rememberCameraPositionState
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.FiltroMapa
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.ui.components.BUENOS_AIRES
import com.jorge.cervelog.ui.components.BarraSuperior
import com.jorge.cervelog.ui.components.MensajeSnackbar
import com.jorge.cervelog.ui.components.ZOOM_BARRIO
import com.jorge.cervelog.ui.components.ZOOM_CIUDAD
import com.jorge.cervelog.ui.components.rememberPedirUbicacion
import com.jorge.cervelog.ui.components.tienePermisoUbicacion
import com.jorge.cervelog.ui.components.toLatLng
import com.jorge.cervelog.ui.components.toZona
import com.jorge.cervelog.ui.theme.AppTheme

// Adaptador para el clustering
private class MarcadorPublicacion(val publicacion: Publicacion, autor: String) : ClusterItem {
    override val position = publicacion.ubicacion.toLatLng()
    override val title = publicacion.nombre
    override val snippet = "${"★".repeat(publicacion.puntuacion)} · $autor"
    override val zIndex: Float? = null
}

@OptIn(ExperimentalMaterial3Api::class, MapsComposeExperimentalApi::class)
@Composable
fun MapaComunitarioRoute(
    onVerMiCerveza: (String) -> Unit,
    onVerDetalle: (String) -> Unit,
    viewModel: MapaComunitarioViewModel = viewModel(factory = MapaComunitarioViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
    MensajeSnackbar(state.error, snackbarHostState, viewModel::onErrorMostrado)

    // Solo el id, para que sobreviva al giro de pantalla
    var seleccionadaId by rememberSaveable { mutableStateOf<String?>(null) }
    val seleccionada = state.publicaciones.find { it.id == seleccionadaId }
    var mostrarFiltros by rememberSaveable { mutableStateOf(false) }

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
    // Se guarda para no volver a centrar al regresar desde el detalle y respetar la zona que se veía
    var centrado by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(miUbicacion) {
        val posicion = miUbicacion ?: return@LaunchedEffect
        if (centrado) return@LaunchedEffect
        // Sin animación para no disparar varias recargas
        cameraPositionState.move(CameraUpdateFactory.newLatLngZoom(posicion.toLatLng(), ZOOM_BARRIO))
        centrado = true
    }
    val cargarZonaVisible = {
        cameraPositionState.projection?.visibleRegion?.latLngBounds?.let { viewModel.onZonaVisible(it.toZona()) }
    }
    LaunchedEffect(cameraPositionState.isMoving) {
        if (!cameraPositionState.isMoving) cargarZonaVisible()
    }

    val tu = stringResource(R.string.tu)
    val visibles = remember(state.publicaciones, state.filtro) { state.visibles }
    // Google Maps guarda cada marcador ya dibujado: con objetos nuevos se repintan con los colores del tema
    val oscuro = AppTheme.oscuro
    val marcadores = remember(visibles, oscuro) {
        visibles.map {
            MarcadorPublicacion(it, autor = if (it.autorUid == viewModel.uidUsuario) tu else it.autorEmail)
        }
    }

    Scaffold(
        topBar = {
            BarraSuperior(
                titulo = stringResource(R.string.mapa_cervecero),
                actions = { BotonFiltros(state.filtro.activos) { mostrarFiltros = true } },
            )
        },
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
                mapColorScheme = if (AppTheme.oscuro) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
                properties = MapProperties(isMyLocationEnabled = permisoUbicacion),
                onMapLoaded = { cargarZonaVisible() },
            ) {
                // Sin animación porque los marcadores tiemblan al redibujarse
                val clusterManager = rememberClusterManager<MarcadorPublicacion>()
                // Cada cerveza se pinta con su nombre; los grupos siguen con la burbuja de Google
                val renderer = rememberClusterRenderer(
                    clusterContent = null,
                    clusterItemContent = { EtiquetaMarcador(it.publicacion.nombre) },
                    clusterManager = clusterManager,
                )
                if (clusterManager != null && renderer != null) {
                    SideEffect {
                        if (clusterManager.renderer != renderer) clusterManager.renderer = renderer
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
            if (visibles.isEmpty() && state.publicaciones.isNotEmpty()) {
                AvisoSinResultados(
                    onLimpiar = { viewModel.onFiltroCambiado(FiltroMapa()) },
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(AppTheme.spacing.lg),
                )
            }
        }
    }

    if (mostrarFiltros) {
        ModalBottomSheet(onDismissRequest = { mostrarFiltros = false }) {
            FiltrosMapa(
                filtro = state.filtro,
                estilosDisponibles = state.estilosDisponibles,
                resultados = visibles.size,
                onFiltroCambiado = viewModel::onFiltroCambiado,
                onCerrar = { mostrarFiltros = false },
            )
        }
    }

    if (seleccionada != null) {
        ModalBottomSheet(onDismissRequest = { seleccionadaId = null }) {
            HojaPublicacion(
                publicacion = seleccionada,
                esMia = seleccionada.autorUid == viewModel.uidUsuario,
                onVerDetalle = {
                    seleccionadaId = null
                    onVerDetalle(seleccionada.id)
                },
                onVerMiCerveza = {
                    seleccionadaId = null
                    onVerMiCerveza(seleccionada.id)
                },
            )
        }
    }
}

// Los filtros dejan el mapa vacío en esta zona
@Composable
private fun AvisoSinResultados(onLimpiar: () -> Unit, modifier: Modifier = Modifier) {
    Card(modifier, elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)) {
        Row(
            modifier = Modifier.padding(start = AppTheme.spacing.lg, end = AppTheme.spacing.xs),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Deja sitio al botón aunque el texto no quepa en una línea
            Text(
                stringResource(R.string.sin_resultados_filtros),
                style = AppTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f, fill = false),
            )
            TextButton(onClick = onLimpiar) { Text(stringResource(R.string.limpiar)) }
        }
    }
}

@Composable
private fun HojaPublicacion(
    publicacion: Publicacion,
    esMia: Boolean,
    onVerDetalle: () -> Unit,
    onVerMiCerveza: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = AppTheme.spacing.xl, end = AppTheme.spacing.xl, bottom = AppTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        DatosPublicacion(publicacion, esMia)
        Button(onClick = onVerDetalle, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.ver_detalle))
        }
        if (esMia) {
            OutlinedButton(onClick = onVerMiCerveza, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.ver_mi_cerveza))
            }
        }
    }
}
