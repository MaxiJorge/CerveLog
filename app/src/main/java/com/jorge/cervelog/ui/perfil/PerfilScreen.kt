package com.jorge.cervelog.ui.perfil

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.Estadisticas
import com.jorge.cervelog.ui.components.AvatarCerveza
import com.jorge.cervelog.ui.components.BarraSuperior
import com.jorge.cervelog.ui.components.DialogoConfirmacion
import com.jorge.cervelog.ui.components.MensajeSnackbar
import com.jorge.cervelog.ui.components.TarjetaCifra
import com.jorge.cervelog.ui.components.formatearDecimal
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme

@Composable
fun PerfilRoute(viewModel: PerfilViewModel = viewModel(factory = PerfilViewModel.Factory)) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    PerfilScreen(
        state = state,
        onReenviarCorreo = viewModel::onReenviarCorreo,
        onErrorMostrado = viewModel::onErrorMostrado,
        onCerrarSesion = viewModel::onCerrarSesion,
    )
}

@Composable
fun PerfilScreen(
    state: PerfilUiState,
    onReenviarCorreo: () -> Unit,
    onErrorMostrado: () -> Unit,
    onCerrarSesion: () -> Unit,
) {
    var confirmarLogout by rememberSaveable { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    MensajeSnackbar(state.error, snackbarHostState, onErrorMostrado)

    Scaffold(
        topBar = { BarraSuperior(stringResource(R.string.mi_perfil)) },
        snackbarHost = { SnackbarHost(snackbarHostState) },
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .padding(AppTheme.spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg),
        ) {
            AvatarCerveza(tamano = 96.dp, estilo = AppTheme.typography.displaySmall)
            Text(state.email, style = AppTheme.typography.titleMedium)
            if (!state.emailVerificado) {
                AvisoVerificacion(state.enviandoCorreo, state.correoEnviado, onReenviarCorreo)
            }

            state.estadisticas?.let { e ->
                Row(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
                    TarjetaCifra(stringResource(R.string.cervezas), e.total.toString(), Modifier.weight(1f))
                    TarjetaCifra(stringResource(R.string.en_el_mapa), e.enElMapa.toString(), Modifier.weight(1f))
                    TarjetaCifra(
                        stringResource(R.string.nota_media),
                        e.puntuacionMedia?.formatearDecimal() ?: stringResource(R.string.sin_dato),
                        Modifier.weight(1f),
                    )
                }
            }

            OutlinedButton(
                onClick = { confirmarLogout = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = AppTheme.spacing.sm),
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.cerrar_sesion), modifier = Modifier.padding(start = AppTheme.spacing.sm))
            }
        }
    }

    if (confirmarLogout) {
        DialogoConfirmacion(
            titulo = stringResource(R.string.cerrar_sesion),
            texto = stringResource(R.string.cerrar_sesion_texto),
            textoConfirmar = stringResource(R.string.salir),
            onConfirmar = {
                confirmarLogout = false
                onCerrarSesion()
            },
            onCancelar = { confirmarLogout = false },
        )
    }
}

@Composable
private fun AvisoVerificacion(enviando: Boolean, enviado: Boolean, onReenviar: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            stringResource(if (enviado) R.string.correo_reenviado else R.string.correo_sin_verificar),
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (!enviado) {
            TextButton(onClick = onReenviar, enabled = !enviando) {
                Text(stringResource(R.string.reenviar_correo))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PerfilScreenPreview() {
    CerveLogTheme {
        PerfilScreen(
            PerfilUiState(
                email = "ana@aplicaciones.com",
                emailVerificado = false,
                estadisticas = Estadisticas(total = 4, enElMapa = 3, puntuacionMedia = 3.75),
            ),
            onReenviarCorreo = {},
            onErrorMostrado = {},
            onCerrarSesion = {},
        )
    }
}
