package com.jorge.cervelog

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.jorge.cervelog.domain.model.Tema
import com.jorge.cervelog.ui.navigation.AppNavigation
import com.jorge.cervelog.ui.theme.CerveLogTheme
import com.jorge.cervelog.ui.theme.TemaViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val temaViewModel: TemaViewModel = viewModel(factory = TemaViewModel.Factory)
            val tema by temaViewModel.tema.collectAsStateWithLifecycle()
            val oscuro = when (tema) {
                Tema.SISTEMA -> isSystemInDarkTheme()
                Tema.CLARO -> false
                Tema.OSCURO -> true
            }

            // Los iconos de la barra de estado tienen que seguir al tema elegido, no al del sistema
            DisposableEffect(oscuro) {
                val estilo = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { oscuro }
                enableEdgeToEdge(statusBarStyle = estilo, navigationBarStyle = estilo)
                onDispose {}
            }

            CerveLogTheme(darkTheme = oscuro) {
                AppNavigation()
            }
        }
    }
}
