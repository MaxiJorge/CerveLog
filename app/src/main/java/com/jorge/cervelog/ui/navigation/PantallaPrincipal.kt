package com.jorge.cervelog.ui.navigation

import androidx.activity.compose.BackHandler
import androidx.annotation.StringRes
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
import androidx.compose.ui.res.stringResource
import com.jorge.cervelog.R
import com.jorge.cervelog.ui.cervezas.list.CervezasListRoute
import com.jorge.cervelog.ui.mapa.MapaComunitarioRoute
import com.jorge.cervelog.ui.perfil.PerfilRoute

private enum class Pestana(@StringRes val titulo: Int, val icono: ImageVector) {
    CERVEZAS(R.string.cervezas, Icons.AutoMirrored.Filled.List),
    MAPA(R.string.mapa, Icons.Filled.Place),
    PERFIL(R.string.perfil, Icons.Filled.Person),
}

// Las pestañas no son destinos del NavHost para que el mapa no se destruya al cambiar.
// Se crea al abrirlo y después se queda debajo de la pestaña activa.
@Composable
fun PantallaPrincipal(
    onCervezaClick: (String) -> Unit,
    onAgregarClick: () -> Unit,
    onPublicacionClick: (String) -> Unit,
) {
    var pestana by rememberSaveable { mutableStateOf(Pestana.CERVEZAS) }
    var mapaAbierto by rememberSaveable { mutableStateOf(false) }

    BackHandler(enabled = pestana != Pestana.CERVEZAS) { pestana = Pestana.CERVEZAS }

    // Cada pantalla gestiona sus márgenes; aquí solo se reserva la barra
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
                        label = { Text(stringResource(p.titulo)) },
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
            if (mapaAbierto) MapaComunitarioRoute(onVerMiCerveza = onCervezaClick, onVerDetalle = onPublicacionClick)
            when (pestana) {
                Pestana.CERVEZAS -> CervezasListRoute(onCervezaClick, onAgregarClick)
                Pestana.PERFIL -> PerfilRoute()
                Pestana.MAPA -> Unit
            }
        }
    }
}
