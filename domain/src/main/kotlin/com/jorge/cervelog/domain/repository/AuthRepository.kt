package com.jorge.cervelog.domain.repository

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Usuario
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val usuarioActual: Usuario?

    fun observarSesion(): Flow<Usuario?>

    suspend fun iniciarSesion(email: String, contrasena: String): AppResult<Unit>

    suspend fun iniciarSesionConGoogle(idToken: String): AppResult<Unit>

    suspend fun registrarse(email: String, contrasena: String): AppResult<Unit>

    suspend fun enviarCorreoVerificacion(): AppResult<Unit>

    // Vuelve a leer el usuario del servidor para saber si ya ha confirmado el correo
    suspend fun recargarUsuario(): AppResult<Usuario>

    fun cerrarSesion()
}
