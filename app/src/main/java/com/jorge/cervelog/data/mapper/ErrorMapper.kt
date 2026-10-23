package com.jorge.cervelog.data.mapper

import com.google.firebase.FirebaseNetworkException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.firestore.FirebaseFirestoreException
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import kotlinx.coroutines.CancellationException
import java.io.IOException

class SinSesionException : IllegalStateException("No hay un usuario autenticado")

class RutaNoDisponibleException : IllegalStateException("No se pudo calcular la ruta")

fun Throwable.toDomainError(): DomainError = when (this) {
    // Va primero porque hereda de FirebaseAuthInvalidCredentialsException
    is FirebaseAuthWeakPasswordException -> DomainError.ContrasenaDebil
    is FirebaseAuthUserCollisionException -> DomainError.EmailYaRegistrado
    is FirebaseAuthInvalidUserException,
    is FirebaseAuthInvalidCredentialsException -> DomainError.CredencialesIncorrectas
    is FirebaseNetworkException -> DomainError.SinConexion
    is FirebaseTooManyRequestsException -> DomainError.DemasiadosIntentos
    is FirebaseFirestoreException -> when (code) {
        FirebaseFirestoreException.Code.UNAVAILABLE -> DomainError.SinConexion
        FirebaseFirestoreException.Code.PERMISSION_DENIED -> DomainError.SinPermiso
        FirebaseFirestoreException.Code.NOT_FOUND -> DomainError.NoEncontrada
        FirebaseFirestoreException.Code.UNAUTHENTICATED -> DomainError.SinSesion
        else -> DomainError.Desconocido(message)
    }
    is SinSesionException -> DomainError.SinSesion
    is RutaNoDisponibleException -> DomainError.RutaNoDisponible
    is IOException -> DomainError.SinConexion
    is SecurityException -> DomainError.SinPermisoUbicacion
    else -> DomainError.Desconocido(message)
}

suspend fun <T> resultadoDe(llamada: suspend () -> T): AppResult<T> = try {
    AppResult.Success(llamada())
} catch (e: CancellationException) {
    throw e
} catch (e: Exception) {
    AppResult.Error(e.toDomainError())
}
