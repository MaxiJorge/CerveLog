package com.jorge.cervelog.ui.auth

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.FirebaseNetworkException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.jorge.cervelog.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class AuthUiState(
    val loading: Boolean = false,
    val error: String? = null,
)

class AuthViewModel(
    private val repository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthUiState())
    val uiState: StateFlow<AuthUiState> = _uiState.asStateFlow()

    fun login(email: String, password: String) {
        val emailLimpio = email.trim()
        val errorValidacion = when {
            !esEmailValido(emailLimpio) -> "Introduce un email válido"
            password.isEmpty() -> "Introduce tu contraseña"
            else -> null
        }
        ejecutar(errorValidacion) { repository.login(emailLimpio, password) }
    }

    fun register(email: String, password: String, confirmacion: String) {
        val emailLimpio = email.trim()
        val errorValidacion = when {
            !esEmailValido(emailLimpio) -> "Introduce un email válido"
            password.length < 6 -> "La contraseña debe tener al menos 6 caracteres"
            password != confirmacion -> "Las contraseñas no coinciden"
            else -> null
        }
        ejecutar(errorValidacion) { repository.register(emailLimpio, password) }
    }

    fun errorMostrado() = _uiState.update { it.copy(error = null) }

    // No hace falta navegar: la navegación reacciona al cambio de sesión
    private fun ejecutar(errorValidacion: String?, accion: suspend () -> Unit) {
        if (errorValidacion != null) {
            _uiState.update { it.copy(error = errorValidacion) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            try {
                accion()
                _uiState.update { it.copy(loading = false) }
            } catch (e: Exception) {
                _uiState.update { it.copy(loading = false, error = traducirError(e)) }
            }
        }
    }

    private fun esEmailValido(email: String) = Patterns.EMAIL_ADDRESS.matcher(email).matches()

    private fun traducirError(e: Exception): String = when (e) {
        is FirebaseAuthWeakPasswordException -> "La contraseña es demasiado débil"
        is FirebaseAuthUserCollisionException -> "Ya existe una cuenta con ese email"
        is FirebaseAuthInvalidUserException,
        is FirebaseAuthInvalidCredentialsException -> "Email o contraseña incorrectos"
        is FirebaseNetworkException -> "Sin conexión. Revisa tu internet e inténtalo de nuevo"
        else -> e.localizedMessage ?: "Ocurrió un error inesperado"
    }

    companion object {
        val Factory = viewModelFactory {
            initializer { AuthViewModel(AuthRepository()) }
        }
    }
}
