package com.jorge.cervelog.ui.navigation

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.jorge.cervelog.CerveLogApp
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.Usuario
import com.jorge.cervelog.domain.usecase.ObservarSesionUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUsuarioActualUseCase
import com.jorge.cervelog.notificaciones.AvisoCervezasCercanas
import com.jorge.cervelog.ui.auth.LoginRoute
import com.jorge.cervelog.ui.auth.RegisterRoute
import com.jorge.cervelog.ui.cervezas.detail.CervezaDetailRoute
import com.jorge.cervelog.ui.cervezas.form.CervezaFormRoute
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.Serializable

@Serializable data object Login
@Serializable data object Registro

@Serializable data object Principal
@Serializable data class DetalleCerveza(val cervezaId: String)

@Serializable data class FormularioCerveza(val cervezaId: String? = null)

class SesionViewModel(
    observarSesion: ObservarSesionUseCase,
    obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
) : ViewModel() {
    val usuario: StateFlow<Usuario?> = observarSesion()
        .stateIn(viewModelScope, SharingStarted.Eagerly, obtenerUsuarioActual())

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                SesionViewModel(c.observarSesion, c.obtenerUsuarioActual)
            }
        }
    }
}

// Sin sesión no se puede llegar a las pantallas principales
@Composable
fun AppNavigation(viewModel: SesionViewModel = viewModel(factory = SesionViewModel.Factory)) {
    val usuario by viewModel.usuario.collectAsStateWithLifecycle()

    val uid = usuario?.uid
    if (uid == null) {
        AuthNavHost()
    } else {
        // Si cambia el usuario se recrea el estado del grafo
        key(uid) { MainNavHost() }
    }
}

@Composable
private fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Login) {
        composable<Login> {
            LoginRoute(onIrARegistro = { navController.navigate(Registro) })
        }
        composable<Registro> {
            RegisterRoute(onIrALogin = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()
    AvisosCervezasCercanas()
    // Sin transiciones porque el fundido hace parpadear el mapa
    NavHost(
        navController,
        startDestination = Principal,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable<Principal> {
            PantallaPrincipal(
                onCervezaClick = { navController.navigate(DetalleCerveza(it)) },
                onAgregarClick = { navController.navigate(FormularioCerveza()) },
            )
        }
        composable<DetalleCerveza> {
            CervezaDetailRoute(
                onBack = { navController.popBackStack() },
                onEditar = { navController.navigate(FormularioCerveza(it)) },
            )
        }
        composable<FormularioCerveza> {
            // La pantalla anterior se actualiza sola porque escucha Firestore
            CervezaFormRoute(
                onBack = { navController.popBackStack() },
                onGuardada = { navController.popBackStack() },
            )
        }
    }
}

@Composable
private fun AvisosCervezasCercanas() {
    val app = LocalContext.current.applicationContext as CerveLogApp
    // Desde Android 13 hace falta permiso; si se deniega, no hay avisos
    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        AvisoCervezasCercanas(app, app.container.observarCervezasCercanas).escuchar()
    }
}
