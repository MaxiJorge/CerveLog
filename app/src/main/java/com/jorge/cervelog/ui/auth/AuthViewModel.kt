package com.jorge.cervelog.ui.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.usecase.IniciarSesionConGoogleUseCase
import com.jorge.cervelog.domain.usecase.IniciarSesionUseCase
import com.jorge.cervelog.domain.usecase.RegistrarseUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val cargando: Boolean = false,
    val error: DomainError? = null,
)

class AuthViewModel(
    private val iniciarSesion: IniciarSesionUseCase,
    private val registrarse: RegistrarseUseCase,
    private val iniciarSesionConGoogle: IniciarSesionConGoogleUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun onIniciarSesion(email: String, contrasena: String) = ejecutar { iniciarSesion(email, contrasena) }

    fun onRegistrarse(email: String, contrasena: String, confirmacion: String) =
        ejecutar { registrarse(email, contrasena, confirmacion) }

    fun onIniciarSesionConGoogle(idToken: String) = ejecutar { iniciarSesionConGoogle(idToken) }

    fun onErrorGoogle(error: DomainError) = _uiState.update { it.copy(error = error) }

    fun onErrorMostrado() = _uiState.update { it.copy(error = null) }

    // No navega: la navegación reacciona al cambio de sesión
    private fun ejecutar(accion: suspend () -> AppResult<Unit>) {
        if (_uiState.value.cargando) return
        viewModelScope.launch {
            _uiState.update { it.copy(cargando = true, error = null) }
            val resultado = accion()
            _uiState.update { it.copy(cargando = false, error = (resultado as? AppResult.Error)?.error) }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                AuthViewModel(c.iniciarSesion, c.registrarse, c.iniciarSesionConGoogle)
            }
        }
    }
}
