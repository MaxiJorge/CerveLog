package com.jorge.cervelog.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.jorge.cervelog.data.repository.AuthRepository
import com.jorge.cervelog.ui.auth.LoginScreen
import com.jorge.cervelog.ui.auth.RegisterScreen
import com.jorge.cervelog.ui.cervezas.detail.CervezaDetailScreen
import com.jorge.cervelog.ui.cervezas.form.CervezaFormScreen
import com.jorge.cervelog.ui.cervezas.list.CervezasListScreen

object Routes {
    const val LOGIN = "login"
    const val REGISTER = "register"
    const val CERVEZAS = "cervezas"
    const val ARG_CERVEZA_ID = "cervezaId"
    const val CERVEZA_DETAIL = "cervezas/{$ARG_CERVEZA_ID}"

    // Ruta aparte (no "cervezas/...") para que no se confunda con el detalle.
    // Sin id = crear; con id = editar.
    const val CERVEZA_FORM = "cerveza_form?$ARG_CERVEZA_ID={$ARG_CERVEZA_ID}"

    fun cervezaDetail(id: String) = "cervezas/$id"
    fun cervezaForm(id: String? = null) = if (id == null) "cerveza_form" else "cerveza_form?$ARG_CERVEZA_ID=$id"
}

/**
 * Punto de entrada de la UI. Según haya o no sesión iniciada muestra uno de dos grafos
 * de navegación separados: las pantallas principales solo existen dentro del grafo
 * autenticado, así que no se puede llegar a ellas sin iniciar sesión (RF1).
 * Al cerrar sesión el grafo principal se descarta entero, junto con su back stack.
 */
@Composable
fun AppNavigation() {
    val authRepository = remember { AuthRepository() }
    val user by authRepository.authState.collectAsStateWithLifecycle(authRepository.currentUser)

    val uid = user?.uid
    if (uid == null) {
        AuthNavHost()
    } else {
        // key(uid): si cambia el usuario se recrea todo el estado del grafo principal.
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
    NavHost(navController, startDestination = Routes.CERVEZAS) {
        composable(Routes.CERVEZAS) {
            CervezasListScreen(
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
            // Al guardar se vuelve a la pantalla anterior (listado o detalle), que se
            // actualiza sola porque escucha Firestore en tiempo real.
            CervezaFormScreen(
                onBack = { navController.popBackStack() },
                onGuardada = { navController.popBackStack() },
            )
        }
    }
}
