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
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jorge.cervelog.data.repository.AuthRepository
import com.jorge.cervelog.notificaciones.AvisoCervezasCercanas
import com.jorge.cervelog.ui.auth.LoginScreen
import com.jorge.cervelog.ui.auth.RegisterScreen
import com.jorge.cervelog.ui.cervezas.detail.CervezaDetailScreen
import com.jorge.cervelog.ui.cervezas.form.CervezaFormScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    // Las pestañas Cervezas, Mapa y Perfil (ver PantallaPrincipal)
    const val PRINCIPAL = "principal"
    const val ARG_CERVEZA_ID = "cervezaId"
    const val CERVEZA_DETAIL = "cervezas/{$ARG_CERVEZA_ID}"

    // Sin id = crear; con id = editar.
    const val CERVEZA_FORM = "cerveza_form?$ARG_CERVEZA_ID={$ARG_CERVEZA_ID}"

    fun cervezaDetail(id: String) = "cervezas/$id"
    fun cervezaForm(id: String? = null) = if (id == null) "cerveza_form" else "cerveza_form?$ARG_CERVEZA_ID=$id"
}

// Dos grafos separados: sin sesión no se puede llegar a las pantallas principales
@Composable
fun AppNavigation() {
    val authRepository = remember { AuthRepository() }
    val user by authRepository.authState.collectAsStateWithLifecycle(authRepository.currentUser)

    val uid = user?.uid
    if (uid == null) {
        AuthNavHost()
    } else {
        // Si cambia el usuario se recrea todo el estado del grafo
        key(uid) { MainNavHost() }
    }
}

@Composable
private fun AuthNavHost() {
    val navController = rememberNavController()
    NavHost(navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(onIrARegistro = { navController.navigate(Routes.REGISTER) })
        }
        composable(Routes.REGISTER) {
            RegisterScreen(onIrALogin = { navController.popBackStack() })
        }
    }
}

@Composable
private fun MainNavHost() {
    val navController = rememberNavController()
    AvisosCervezasCercanas()
    // Sin el fundido por defecto: el mapa de Google parpadea si se anima su transparencia
    NavHost(
        navController,
        startDestination = Routes.PRINCIPAL,
        enterTransition = { EnterTransition.None },
        exitTransition = { ExitTransition.None },
    ) {
        composable(Routes.PRINCIPAL) {
            PantallaPrincipal(
                onCervezaClick = { navController.navigate(Routes.cervezaDetail(it)) },
                onAgregarClick = { navController.navigate(Routes.cervezaForm()) },
            )
        }
        composable(
            route = Routes.CERVEZA_DETAIL,
            arguments = listOf(navArgument(Routes.ARG_CERVEZA_ID) { type = NavType.StringType }),
        ) {
            CervezaDetailScreen(
                onBack = { navController.popBackStack() },
                onEditar = { navController.navigate(Routes.cervezaForm(it)) },
            )
        }
        composable(
            route = Routes.CERVEZA_FORM,
            arguments = listOf(
                navArgument(Routes.ARG_CERVEZA_ID) {
                    type = NavType.StringType
                    nullable = true
                    defaultValue = null
                },
            ),
        ) {
            // La pantalla anterior se actualiza sola porque escucha Firestore
            CervezaFormScreen(
                onBack = { navController.popBackStack() },
                onGuardada = { navController.popBackStack() },
            )
        }
    }
}

// Mientras haya sesión se escuchan las cervezas que publican otros cerca (RF7)
@Composable
private fun AvisosCervezasCercanas() {
    val context = LocalContext.current.applicationContext
    // Desde Android 13 las notificaciones necesitan permiso; si lo deniega, no hay avisos
    val permisoLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { }
    LaunchedEffect(Unit) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permisoLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
        AvisoCervezasCercanas(context).escuchar()
    }
}
