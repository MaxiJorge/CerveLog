package com.jorge.cervelog.ui.auth

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun RegisterScreen(
    onIrALogin: () -> Unit,
    viewModel: AuthViewModel = viewModel(factory = AuthViewModel.Factory),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var email by rememberSaveable { mutableStateOf("") }
    var password by rememberSaveable { mutableStateOf("") }
    var confirmacion by rememberSaveable { mutableStateOf("") }
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.error) {
        val texto = state.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(texto)
        viewModel.errorMostrado()
    }

    Scaffold(snackbarHost = { SnackbarHost(snackbarHostState) }) { padding ->
        AuthScaffold(
            subtitulo = "Crea tu cuenta",
            modifier = Modifier.padding(padding),
        ) {
            AuthTextField(
                value = email,
                onValueChange = { email = it },
                label = "Email",
                keyboardType = KeyboardType.Email,
                enabled = !state.loading,
            )
            AuthTextField(
                value = password,
                onValueChange = { password = it },
                label = "Contraseña (mínimo 6 caracteres)",
                keyboardType = KeyboardType.Password,
                isPassword = true,
                enabled = !state.loading,
            )
            AuthTextField(
                value = confirmacion,
                onValueChange = { confirmacion = it },
                label = "Repetir contraseña",
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
                isPassword = true,
                enabled = !state.loading,
            )
            AuthPrimaryButton(text = "Crear cuenta", loading = state.loading) {
                viewModel.register(email, password, confirmacion)
            }
            TextButton(onClick = onIrALogin, enabled = !state.loading) {
                Text("¿Ya tienes cuenta? Inicia sesión")
            }
        }
    }
}
