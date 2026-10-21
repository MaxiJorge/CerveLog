package com.jorge.cervelog.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import com.jorge.cervelog.ui.cervezas.list.CervezasListScreen
import com.jorge.cervelog.ui.mapa.MapaComunitarioScreen
import com.jorge.cervelog.ui.perfil.PerfilScreen

private enum class Pestana(val titulo: String, val icono: ImageVector) {
    CERVEZAS("Cervezas", Icons.AutoMirrored.Filled.List),
    MAPA("Mapa", Icons.Filled.Place),
    PERFIL("Perfil", Icons.Filled.Person),
}

// Las pestañas no son destinos del NavHost: así el mapa no se destruye al cambiar de pestaña.
// Se crea la primera vez que se abre y después se queda debajo, tapado por la pestaña activa.
@Composable
fun PantallaPrincipal(
    onCervezaClick: (String) -> Unit,
    onAgregarClick: () -> Unit,
) {
    var pestana by rememberSaveable { mutableStateOf(Pestana.CERVEZAS) }
    var mapaAbierto by rememberSaveable { mutableStateOf(false) }

    // Atrás desde Mapa o Perfil vuelve a Cervezas en vez de salir de la app
    BackHandler(enabled = pestana != Pestana.CERVEZAS) { pestana = Pestana.CERVEZAS }

    // Cada pantalla gestiona sus propios márgenes del sistema; aquí solo se reserva la barra
    Scaffold(
        contentWindowInsets = WindowInsets(0),
        bottomBar = {
            NavigationBar {
                Pestana.entries.forEach { p ->
                    NavigationBarItem(
                        selected = pestana == p,
                        onClick = {
                            pestana = p
                            if (p == Pestana.MAPA) mapaAbierto = true
                        },
                        icon = { Icon(p.icono, contentDescription = null) },
                        label = { Text(p.titulo) },
                    )
                }
            }
        },
    ) { padding ->
        Box(
            Modifier
                .padding(padding)
                .consumeWindowInsets(padding),
        ) {
            if (mapaAbierto) MapaComunitarioScreen(onVerMiCerveza = onCervezaClick)
            when (pestana) {
                Pestana.CERVEZAS -> CervezasListScreen(onCervezaClick, onAgregarClick)
                Pestana.PERFIL -> PerfilScreen()
                Pestana.MAPA -> Unit
            }
        }
    }
}
