package com.jorge.cervelog.ui.components

import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.res.stringResource
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.DomainError

@Composable
fun DomainError.asMessage(): String = stringResource(
    when (this) {
        DomainError.SinConexion -> R.string.error_sin_conexion
        DomainError.SinPermiso -> R.string.error_sin_permiso
        DomainError.NoEncontrada -> R.string.error_no_encontrada
        DomainError.SinSesion -> R.string.error_sin_sesion
        DomainError.EmailNoValido -> R.string.error_email_no_valido
        DomainError.ContrasenaVacia -> R.string.error_contrasena_vacia
        DomainError.ContrasenaCorta -> R.string.error_contrasena_corta
        DomainError.ContrasenasDistintas -> R.string.error_contrasenas_distintas
        DomainError.ContrasenaDebil -> R.string.error_contrasena_debil
        DomainError.CredencialesIncorrectas -> R.string.error_credenciales
        DomainError.EmailYaRegistrado -> R.string.error_email_registrado
        DomainError.GoogleNoDisponible -> R.string.error_google_no_disponible
        DomainError.GoogleNoConfigurado -> R.string.error_google_no_configurado
        DomainError.DemasiadosIntentos -> R.string.error_demasiados_intentos
        DomainError.NombreVacio -> R.string.error_nombre_vacio
        DomainError.PuntuacionNoValida -> R.string.error_puntuacion
        DomainError.AbvNoValido -> R.string.error_abv
        DomainError.IbuNoValido -> R.string.error_ibu
        DomainError.UbicacionNoDisponible -> R.string.error_ubicacion_no_disponible
        DomainError.SinPermisoUbicacion -> R.string.error_sin_permiso_ubicacion
        DomainError.FotoNoValida -> R.string.error_foto
        DomainError.CamaraNoDisponible -> R.string.error_camara
        DomainError.RutaNoDisponible -> R.string.error_ruta_no_disponible
        is DomainError.Desconocido -> R.string.error_desconocido
    },
)

@Composable
fun MensajeSnackbar(error: DomainError?, snackbarHostState: SnackbarHostState, onMostrado: () -> Unit) {
    val texto = error?.asMessage()
    LaunchedEffect(error) {
        if (texto == null) return@LaunchedEffect
        snackbarHostState.showSnackbar(texto)
        onMostrado()
    }
}
