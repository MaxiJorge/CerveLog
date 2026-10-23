package com.jorge.cervelog.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider

// Se definen todos los roles: los que faltan Material los rellena con su morado por defecto
private val LightColorScheme = lightColorScheme(
    primary = Amber40,
    onPrimary = White,
    primaryContainer = AmberContainer90,
    onPrimaryContainer = OnAmberContainer10,
    inversePrimary = Amber80,
    secondary = Malt40,
    onSecondary = White,
    secondaryContainer = MaltContainer90,
    onSecondaryContainer = OnMaltContainer10,
    tertiary = Hop40,
    onTertiary = White,
    tertiaryContainer = HopContainer90,
    onTertiaryContainer = OnHopContainer10,
    background = Cream99,
    onBackground = Stout10,
    surface = Cream99,
    onSurface = Stout10,
    surfaceVariant = Roast90,
    onSurfaceVariant = Roast30,
    surfaceDim = CreamDim87,
    surfaceBright = Cream99,
    surfaceContainerLowest = White,
    surfaceContainerLow = CreamContainer97,
    surfaceContainer = CreamContainer94,
    surfaceContainerHigh = CreamContainer92,
    surfaceContainerHighest = CreamContainer90,
    outline = Roast50,
    outlineVariant = Roast80,
    inverseSurface = StoutContainer22,
    inverseOnSurface = Foam90,
)

// En oscuro el ámbar se aclara: un color saturado sobre fondo oscuro cansa la vista
private val DarkColorScheme = darkColorScheme(
    primary = Amber80,
    onPrimary = OnAmber20,
    primaryContainer = AmberContainer30,
    onPrimaryContainer = OnAmberContainer90,
    inversePrimary = Amber40,
    secondary = Malt80,
    onSecondary = OnMalt20,
    secondaryContainer = MaltContainer30,
    onSecondaryContainer = OnMaltContainer90,
    tertiary = Hop80,
    onTertiary = OnHop20,
    tertiaryContainer = HopContainer30,
    onTertiaryContainer = HopContainer90,
    background = Stout10,
    onBackground = Foam90,
    surface = Stout10,
    onSurface = Foam90,
    surfaceVariant = Roast30,
    onSurfaceVariant = Roast80,
    surfaceDim = Stout10,
    surfaceBright = StoutContainer24,
    surfaceContainerLowest = Stout6,
    surfaceContainerLow = StoutContainer12,
    surfaceContainer = StoutContainer17,
    surfaceContainerHigh = StoutContainer22,
    surfaceContainerHighest = StoutContainer24,
    outline = Roast60,
    outlineVariant = Roast30,
    inverseSurface = Foam90,
    inverseOnSurface = StoutContainer22,
)

// Sin colores dinámicos para que se vea igual en todos los dispositivos
@Composable
fun CerveLogTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(
        LocalAppSpacing provides AppSpacing(),
        LocalTemaOscuro provides darkTheme,
    ) {
        MaterialTheme(
            colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
            typography = Typography,
            content = content
        )
    }
}
