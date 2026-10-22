package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.repository.AuthRepository

// Firebase exige al menos 6 caracteres
const val LONGITUD_MINIMA_CONTRASENA = 6

class RegistrarseUseCase(private val repository: AuthRepository) {
    suspend operator fun invoke(email: String, contrasena: String, confirmacion: String): AppResult<Unit> {
        val emailLimpio = email.trim()
        val error = when {
            !esEmailValido(emailLimpio) -> DomainError.EmailNoValido
            contrasena.length < LONGITUD_MINIMA_CONTRASENA -> DomainError.ContrasenaCorta
            contrasena != confirmacion -> DomainError.ContrasenasDistintas
            else -> null
        }
        if (error != null) return AppResult.Error(error)

        val registro = repository.registrarse(emailLimpio, contrasena)
        // Si falla el envío la cuenta ya existe; el correo se puede reenviar desde el perfil
        if (registro is AppResult.Success) repository.enviarCorreoVerificacion()
        return registro
    }
}
