package com.jorge.cervelog.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Material no trae espaciados
data class AppSpacing(
    val xs: Dp = 4.dp,
    val sm: Dp = 8.dp,
    val md: Dp = 12.dp,
    val lg: Dp = 16.dp,
    val xl: Dp = 24.dp,
    val xxl: Dp = 32.dp,
)

val LocalAppSpacing = staticCompositionLocalOf { AppSpacing() }

// Puede no coincidir con el sistema si se ha elegido el tema a mano en el perfil
val LocalTemaOscuro = staticCompositionLocalOf { false }

object AppTheme {
    val spacing: AppSpacing
        @Composable @ReadOnlyComposable get() = LocalAppSpacing.current

    val colors: ColorScheme
        @Composable @ReadOnlyComposable get() = MaterialTheme.colorScheme

    val typography: Typography
        @Composable @ReadOnlyComposable get() = MaterialTheme.typography

    val oscuro: Boolean
        @Composable @ReadOnlyComposable get() = LocalTemaOscuro.current
}
