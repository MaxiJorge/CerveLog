package com.jorge.cervelog.ui.mapa.detail

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.net.toUri
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.ModoTransporte
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.model.Ruta
import com.jorge.cervelog.ui.components.BarraSuperior
import com.jorge.cervelog.ui.components.LoadingContent
import com.jorge.cervelog.ui.components.MapaRuta
import com.jorge.cervelog.ui.components.MensajeSnackbar
import com.jorge.cervelog.ui.components.MessageContent
import com.jorge.cervelog.ui.components.TarjetaCifra
import com.jorge.cervelog.ui.components.asMessage
import com.jorge.cervelog.ui.components.formatearDuracion
import com.jorge.cervelog.ui.components.formatearKm
import com.jorge.cervelog.ui.components.rememberPedirUbicacion
import com.jorge.cervelog.ui.components.tienePermisoUbicacion
import com.jorge.cervelog.ui.mapa.DatosPublicacion
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme
import java.time.Duration
import java.time.Instant

@Composable
fun PublicacionDetailRoute(
    onBack: () -> Unit,
    onVerMiCerveza: (String) -> Unit,
    viewModel: PublicacionDetailViewModel = viewModel(factory = PublicacionDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val ruta by viewModel.ruta.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var permisoUbicacion by remember { mutableStateOf(tienePermisoUbicacion(context)) }
    val pedirUbicacion = rememberPedirUbicacion { permisoUbicacion = it }
    val cargada = state is PublicacionDetailUiState.Success
    LaunchedEffect(permisoUbicacion, cargada) {
        if (!cargada) return@LaunchedEffect
        if (permisoUbicacion) viewModel.onCalcularRuta() else pedirUbicacion()
    }

    PublicacionDetailScreen(
        state = state,
        ruta = ruta,
        permisoUbicacion = permisoUbicacion,
        onBack = onBack,
        onAbrirEnMaps = { destino -> abrirEnGoogleMaps(context, destino, ruta.ruta?.modo) },
        onVerMiCerveza = onVerMiCerveza,
        onErrorMostrado = viewModel::onErrorMostrado,
    )
}

@Composable
fun PublicacionDetailScreen(
    state: PublicacionDetailUiState,
    ruta: RutaUiState,
    permisoUbicacion: Boolean,
    onBack: () -> Unit,
    onAbrirEnMaps: (Coordenadas) -> Unit,
    onVerMiCerveza: (String) -> Unit,
    onErrorMostrado: () -> Unit,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    MensajeSnackbar(ruta.error, snackbarHostState, onErrorMostrado)
    val publicacion = (state as? PublicacionDetailUiState.Success)?.publicacion

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { BarraSuperior(titulo = publicacion?.nombre ?: stringResource(R.string.detalle), onBack = onBack) },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        when (state) {
            PublicacionDetailUiState.Loading -> LoadingContent(modifier)
            PublicacionDetailUiState.NotFound -> MessageContent(
                emoji = "🔍",
                titulo = stringResource(R.string.publicacion_no_existe),
                descripcion = stringResource(R.string.cerveza_no_existe_descripcion),
                modifier = modifier,
                accion = stringResource(R.string.volver),
                onAccion = onBack,
            )
            is PublicacionDetailUiState.Error -> MessageContent(
                emoji = "⚠️",
                titulo = stringResource(R.string.error_cargar_cerveza),
                descripcion = state.error.asMessage(),
                modifier = modifier,
                accion = stringResource(R.string.volver),
                onAccion = onBack,
            )
            is PublicacionDetailUiState.Success -> PublicacionDetailContent(
                publicacion = state.publicacion,
                esMia = state.esMia,
                ruta = ruta,
                permisoUbicacion = permisoUbicacion,
                onAbrirEnMaps = { onAbrirEnMaps(state.publicacion.ubicacion) },
                onVerMiCerveza = { onVerMiCerveza(state.publicacion.id) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun PublicacionDetailContent(
    publicacion: Publicacion,
    esMia: Boolean,
    ruta: RutaUiState,
    permisoUbicacion: Boolean,
    onAbrirEnMaps: () -> Unit,
    onVerMiCerveza: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
    ) {
        DatosPublicacion(publicacion, esMia)

        Text(stringResource(R.string.como_llegar), style = AppTheme.typography.titleMedium)
        MapaRuta(
            destino = publicacion.ubicacion,
            titulo = publicacion.lugar.ifBlank { publicacion.nombre },
            origen = ruta.origen,
            recorrido = ruta.ruta?.puntos.orEmpty(),
        )
        InfoRuta(ruta, permisoUbicacion)

        Button(onClick = onAbrirEnMaps, modifier = Modifier.fillMaxWidth()) {
            Text(stringResource(R.string.abrir_en_google_maps))
        }
        if (esMia) {
            OutlinedButton(onClick = onVerMiCerveza, modifier = Modifier.fillMaxWidth()) {
                Text(stringResource(R.string.ver_mi_cerveza))
            }
        }
    }
}

@Composable
private fun InfoRuta(ruta: RutaUiState, permisoUbicacion: Boolean) {
    val calculada = ruta.ruta
    when {
        calculada != null -> Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            TarjetaCifra(
                stringResource(R.string.distancia),
                calculada.distanciaMetros.toDouble().formatearKm(),
                Modifier.weight(1f),
            )
            TarjetaCifra(
                stringResource(if (calculada.modo == ModoTransporte.A_PIE) R.string.a_pie else R.string.en_coche),
                calculada.duracion.formatearDuracion(),
                Modifier.weight(1f),
            )
        }
        ruta.calculando -> Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) {
            Text(stringResource(R.string.calculando_ruta), style = AppTheme.typography.bodyMedium)
            LinearProgressIndicator(Modifier.fillMaxWidth())
        }
        !permisoUbicacion -> Text(
            stringResource(R.string.ruta_sin_permiso),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
        )
    }
}

// Abre la app de Google Maps con la navegación; sin ella, la web
private fun abrirEnGoogleMaps(context: Context, destino: Coordenadas, modo: ModoTransporte?) {
    val modoMaps = when (modo) {
        ModoTransporte.A_PIE -> "&travelmode=walking"
        ModoTransporte.EN_COCHE -> "&travelmode=driving"
        null -> ""
    }
    val uri = "https://www.google.com/maps/dir/?api=1&destination=${destino.latitud},${destino.longitud}$modoMaps".toUri()
    try {
        context.startActivity(Intent(Intent.ACTION_VIEW, uri))
    } catch (_: ActivityNotFoundException) {
        // No hay ni Maps ni navegador
    }
}

@Preview(showBackground = true)
@Composable
private fun PublicacionDetailPreview() {
    val destino = Coordenadas(-34.5875, -58.3974)
    CerveLogTheme {
        PublicacionDetailScreen(
            state = PublicacionDetailUiState.Success(
                Publicacion(
                    id = "1",
                    nombre = "Antares Kölsch",
                    cerveceria = "Antares",
                    estilo = "Kölsch",
                    abv = 5.0,
                    puntuacion = 4,
                    lugar = "Recoleta",
                    autorEmail = "ana@aplicaciones.com",
                    ubicacion = destino,
                    fecha = Instant.now(),
                ),
                esMia = false,
            ),
            ruta = RutaUiState(ruta = Ruta(listOf(destino), 2_450, Duration.ofMinutes(9), ModoTransporte.EN_COCHE)),
            permisoUbicacion = true,
            onBack = {},
            onAbrirEnMaps = {},
            onVerMiCerveza = {},
            onErrorMostrado = {},
        )
    }
}
