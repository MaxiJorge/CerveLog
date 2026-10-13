package com.jorge.cervelog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val DarkColorScheme = darkColorScheme(
    primary = Amber80,
    primaryContainer = AmberContainer30,
    onPrimaryContainer = OnAmberContainer90,
    secondary = Malt80,
    secondaryContainer = MaltContainer30,
    onSecondaryContainer = OnMaltContainer90,
    tertiary = Hop80,
    background = Stout10,
    surface = Stout10,
    surfaceContainer = StoutContainer17,
)

private val LightColorScheme = lightColorScheme(
    primary = Amber40,
    primaryContainer = AmberContainer90,
    onPrimaryContainer = OnAmberContainer10,
    secondary = Malt40,
    secondaryContainer = MaltContainer90,
    onSecondaryContainer = OnMaltContainer10,
    tertiary = Hop40,
    background = Cream99,
    surface = Cream99,
    surfaceContainer = CreamContainer94,
)

// Sin colores dinámicos para que la app se vea igual en todos los dispositivos
@Composable
fun CerveLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
