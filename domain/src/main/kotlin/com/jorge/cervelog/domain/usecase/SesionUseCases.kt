package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Usuario
import com.jorge.cervelog.domain.repository.AuthRepository
import kotlinx.coroutines.flow.Flow

class IniciarSesionConGoogleUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(idToken: String): AppResult<Unit> = repository.iniciarSesionConGoogle(idToken)
}

class ReenviarCorreoVerificacionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AppResult<Unit> = repository.enviarCorreoVerificacion()
}

class ComprobarVerificacionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(): AppResult<Usuario> = repository.recargarUsuario()
}

class ObservarSesionUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Flow<Usuario?> = repository.observarSesion()
}

class ObtenerUsuarioActualUseCase(private val repository: AuthRepository) {
    operator fun invoke(): Usuario? = repository.usuarioActual
}

class CerrarSesionUseCase(private val repository: AuthRepository) {
    operator fun invoke() = repository.cerrarSesion()
}
