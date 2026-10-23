package com.jorge.cervelog.domain.model

// Los errores se devuelven como valores, no como excepciones
sealed interface AppResult<out T> {
    data class Success<T>(val data: T) : AppResult<T>
    data class Error(val error: DomainError) : AppResult<Nothing>
}

inline fun <T, R> AppResult<T>.map(transform: (T) -> R): AppResult<R> = when (this) {
    is AppResult.Success -> AppResult.Success(transform(data))
    is AppResult.Error -> this
}

sealed interface DomainError {
    data object SinConexion : DomainError
    data object SinPermiso : DomainError
    data object NoEncontrada : DomainError
    data object SinSesion : DomainError

    data object EmailNoValido : DomainError
    data object ContrasenaVacia : DomainError
    data object ContrasenaCorta : DomainError
    data object ContrasenasDistintas : DomainError
    data object ContrasenaDebil : DomainError
    data object CredencialesIncorrectas : DomainError
    data object EmailYaRegistrado : DomainError
    data object GoogleNoDisponible : DomainError
    data object GoogleNoConfigurado : DomainError
    data object DemasiadosIntentos : DomainError

    data object NombreVacio : DomainError
    data object PuntuacionNoValida : DomainError
    data object AbvNoValido : DomainError
    data object IbuNoValido : DomainError

    data object UbicacionNoDisponible : DomainError
    data object SinPermisoUbicacion : DomainError
    data object FotoNoValida : DomainError
    data object CamaraNoDisponible : DomainError
    data object RutaNoDisponible : DomainError

    data class Desconocido(val mensaje: String?) : DomainError
}
