package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.repository.AuthRepository

class IniciarSesionUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, contrasena: String): AppResult<Unit> {
        val emailLimpio = email.trim()
        val error = when {
            !esEmailValido(emailLimpio) -> DomainError.EmailNoValido
            contrasena.isEmpty() -> DomainError.ContrasenaVacia
            else -> null
        }
        if (error != null) return AppResult.Error(error)
        return repository.iniciarSesion(emailLimpio, contrasena)
    }
}
