package com.jorge.cervelog.ui.auth

import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.usecase.LONGITUD_MINIMA_CONTRASENA
import com.jorge.cervelog.ui.components.BotonConCarga
import com.jorge.cervelog.ui.components.MensajeSnackbar
import com.jorge.cervelog.ui.theme.CerveLogTheme

@Composable
fun RegisterRoute(
    onIrALogin: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val continuarConGoogle = rememberIniciarConGoogle(viewModel::onIniciarSesionConGoogle, viewModel::onErrorGoogle)
    RegisterScreen(
        state = state,
        onRegistrarse = viewModel::onRegistrarse,
        onIrALogin = onIrALogin,
        onContinuarConGoogle = continuarConGoogle,
        onErrorMostrado = viewModel::onErrorMostrado,
    )
}

@Composable
fun RegisterScreen(
    state: AuthUiState,
    onRegistrarse: (email: String, contrasena: String, confirmacion: String) -> Unit,
    onIrALogin: () -> Unit,
    onContinuarConGoogle: () -> Unit,
    onErrorMostrado: () -> Unit,
) {
    var email by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }
    MensajeSnackbar(state.error, snackbarHostState, onErrorMostrado)

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        AuthScaffold(
            subtitulo = stringResource(R.string.registro_subtitulo),
            modifier = Modifier.padding(padding),
        ) {
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = stringResource(R.string.email),
                keyboardType = KeyboardType.Email,
                enabled = !state.cargando,
            )
            AuthTextField(
                value = contrasena,
                onValueChange = { contrasena = it },
                label = stringResource(R.string.contrasena_minimo, LONGITUD_MINIMA_CONTRASENA),
                keyboardType = KeyboardType.Password,
                isPassword = true,
                enabled = !state.cargando,
            )
            AuthTextField(
                value = confirmacion,
                onValueChange = { confirmacion = it },
                label = stringResource(R.string.repetir_contrasena),
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                enabled = !state.cargando,
            )
            BotonConCarga(
                texto = stringResource(R.string.crear_cuenta),
                cargando = state.cargando,
                onClick = { onRegistrarse(email, contrasena, confirmacion) },
            )
            SeparadorO()
            BotonGoogle(enabled = !state.cargando, onClick = onContinuarConGoogle)
            TextButton(onClick = onIrALogin, enabled = !state.cargando) {
                Text(stringResource(R.string.ir_a_login))
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun RegisterScreenPreview() {
    CerveLogTheme {
        RegisterScreen(AuthUiState(), onRegistrarse = { _, _, _ -> }, onIrALogin = {}, onContinuarConGoogle = {}, onErrorMostrado = {})
    }
}
