package com.jorge.cervelog.ui.cervezas.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.ui.components.BarraSuperior
import com.jorge.cervelog.ui.components.DialogoConfirmacion
import com.jorge.cervelog.ui.components.FotoCerveza
import com.jorge.cervelog.ui.components.LoadingContent
import com.jorge.cervelog.ui.components.MapaUbicacion
import com.jorge.cervelog.ui.components.MensajeSnackbar
import com.jorge.cervelog.ui.components.MessageContent
import com.jorge.cervelog.ui.components.RatingStars
import com.jorge.cervelog.ui.components.TarjetaCifra
import com.jorge.cervelog.ui.components.TarjetaDatos
import com.jorge.cervelog.ui.components.asMessage
import com.jorge.cervelog.ui.components.formatearAbv
import com.jorge.cervelog.ui.components.formatearFecha
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme
import java.time.Instant

@Composable
fun CervezaDetailRoute(
    onBack: () -> Unit,
    onEditar: (String) -> Unit,
    viewModel: CervezaDetailViewModel = viewModel(factory = CervezaDetailViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val borrado by viewModel.borrado.collectAsStateWithLifecycle()

    LaunchedEffect(borrado.eliminada) {
        if (borrado.eliminada) onBack()
    }

    CervezaDetailScreen(
        state = state,
        borrado = borrado,
        onBack = onBack,
        onEditar = onEditar,
        onEliminar = viewModel::onEliminar,
        onErrorMostrado = viewModel::onErrorMostrado,
    )
}

@Composable
fun CervezaDetailScreen(
    state: CervezaDetailUiState,
    borrado: BorradoUiState,
    onBack: () -> Unit,
    onEditar: (String) -> Unit,
    onEliminar: () -> Unit,
    onErrorMostrado: () -> Unit,
) {
    var confirmarBorrado by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    MensajeSnackbar(borrado.error, snackbarHostState, onErrorMostrado)
    val cerveza = (state as? CervezaDetailUiState.Success)?.cerveza

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            BarraSuperior(
                titulo = cerveza?.nombre ?: stringResource(R.string.detalle),
                onBack = onBack,
                actions = {
                    if (cerveza != null && !borrado.eliminando) {
                        IconButton(onClick = { onEditar(cerveza.id) }) {
                            Icon(Icons.Filled.Edit, contentDescription = stringResource(R.string.editar))
                        }
                        IconButton(onClick = { confirmarBorrado = true }) {
                            Icon(Icons.Filled.Delete, contentDescription = stringResource(R.string.eliminar))
                        }
                    }
                },
            )
        },
    ) { padding ->
        val modifier = Modifier.padding(padding)
        // Mientras se borra no se muestra el aviso de que ya no existe
        if (borrado.eliminando || borrado.eliminada) {
            LoadingContent(modifier)
            return@Scaffold
        }
        when (state) {
            CervezaDetailUiState.Loading -> LoadingContent(modifier)
            CervezaDetailUiState.NotFound -> MessageContent(
                emoji = "🔍",
                titulo = stringResource(R.string.cerveza_no_existe),
                descripcion = stringResource(R.string.cerveza_no_existe_descripcion),
                modifier = modifier,
                accion = stringResource(R.string.volver),
                onAccion = onBack,
            )
            is CervezaDetailUiState.Error -> MessageContent(
                emoji = "⚠️",
                titulo = stringResource(R.string.error_cargar_cerveza),
                descripcion = state.error.asMessage(),
                modifier = modifier,
                accion = stringResource(R.string.volver),
                onAccion = onBack,
            )
            is CervezaDetailUiState.Success -> CervezaDetailContent(state.cerveza, modifier)
        }
    }

    if (confirmarBorrado) {
        DialogoConfirmacion(
            titulo = stringResource(R.string.eliminar_titulo),
            texto = stringResource(R.string.eliminar_texto),
            textoConfirmar = stringResource(R.string.eliminar),
            onConfirmar = {
                confirmarBorrado = false
                onEliminar()
            },
            onCancelar = { confirmarBorrado = false },
        )
    }
}

@Composable
private fun CervezaDetailContent(cerveza: Cerveza, modifier: Modifier = Modifier) {
    val sinDato = stringResource(R.string.sin_dato)
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(AppTheme.spacing.lg),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val foto = cerveza.foto
            if (foto != null) {
                FotoCerveza(foto, Modifier.padding(bottom = AppTheme.spacing.md))
            } else {
                Text("🍺", style = AppTheme.typography.displayLarge)
            }
            Text(
                cerveza.nombre,
                style = AppTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
            if (cerveza.cerveceria.isNotBlank()) {
                Text(
                    cerveza.cerveceria,
                    style = AppTheme.typography.titleMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
            RatingStars(cerveza.puntuacion, size = 28.dp, modifier = Modifier.padding(top = AppTheme.spacing.sm))
        }

        Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
            TarjetaCifra(stringResource(R.string.abv), cerveza.abv?.formatearAbv() ?: sinDato, Modifier.weight(1f))
            TarjetaCifra(stringResource(R.string.ibu), cerveza.ibu?.toString() ?: sinDato, Modifier.weight(1f))
            TarjetaCifra(
                stringResource(R.string.puntuacion),
                stringResource(R.string.puntuacion_de_5, cerveza.puntuacion),
                Modifier.weight(1f),
            )
        }

        TarjetaDatos(
            stringResource(R.string.estilo) to cerveza.estilo.ifBlank { sinDato },
            stringResource(R.string.fecha) to (cerveza.fecha?.formatearFecha() ?: sinDato),
            stringResource(R.string.lugar) to cerveza.lugar.ifBlank { sinDato },
        )

        if (cerveza.ubicacion != null) {
            MapaUbicacion(ubicacion = cerveza.ubicacion, titulo = cerveza.lugar.ifBlank { cerveza.nombre })
        }

        Card(colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer)) {
            Column(
                Modifier
                    .padding(AppTheme.spacing.lg)
                    .fillMaxWidth(),
            ) {
                Text(
                    stringResource(R.string.notas_cata),
                    style = AppTheme.typography.titleSmall,
                    color = AppTheme.colors.primary,
                )
                Text(
                    cerveza.notas.ifBlank { stringResource(R.string.sin_notas) },
                    style = AppTheme.typography.bodyLarge,
                    modifier = Modifier.padding(top = AppTheme.spacing.xs),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CervezaDetailPreview() {
    CerveLogTheme {
        CervezaDetailScreen(
            state = CervezaDetailUiState.Success(
                Cerveza(
                    id = "1",
                    nombre = "Patagonia Amber Lager",
                    cerveceria = "Patagonia",
                    estilo = "Amber Lager",
                    abv = 5.2,
                    ibu = 22,
                    puntuacion = 4,
                    lugar = "Palermo",
                    notas = "Caramelo suave y final seco.",
                    fecha = Instant.now(),
                ),
            ),
            borrado = BorradoUiState(),
            onBack = {},
            onEditar = {},
            onEliminar = {},
            onErrorMostrado = {},
        )
    }
}
