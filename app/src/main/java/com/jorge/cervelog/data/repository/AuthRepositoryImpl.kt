package com.jorge.cervelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.jorge.cervelog.data.mapper.SinSesionException
import com.jorge.cervelog.data.mapper.resultadoDe
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Usuario
import com.jorge.cervelog.domain.repository.AuthRepository
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(private val auth: FirebaseAuth) : AuthRepository {

    init {
        // Idioma de los correos que envía Firebase
        auth.setLanguageCode("es")
    }

    override val usuarioActual: Usuario? get() = auth.currentUser?.toUsuario()

    override fun observarSesion(): Flow<Usuario?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toUsuario()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    override suspend fun iniciarSesion(email: String, contrasena: String): AppResult<Unit> = resultadoDe<Unit> {
        auth.signInWithEmailAndPassword(email, contrasena).await()
    }

    override suspend fun iniciarSesionConGoogle(idToken: String): AppResult<Unit> = resultadoDe<Unit> {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    override suspend fun registrarse(email: String, contrasena: String): AppResult<Unit> = resultadoDe<Unit> {
        auth.createUserWithEmailAndPassword(email, contrasena).await()
    }

    override suspend fun enviarCorreoVerificacion(): AppResult<Unit> = resultadoDe<Unit> {
        usuario().sendEmailVerification().await()
    }

    override suspend fun recargarUsuario(): AppResult<Usuario> = resultadoDe {
        val usuario = usuario()
        usuario.reload().await()
        usuario.toUsuario()
    }

    override fun cerrarSesion() = auth.signOut()

    private fun usuario(): FirebaseUser = auth.currentUser ?: throw SinSesionException()

    private fun FirebaseUser.toUsuario() = Usuario(uid = uid, email = email.orEmpty(), emailVerificado = isEmailVerified)
}
