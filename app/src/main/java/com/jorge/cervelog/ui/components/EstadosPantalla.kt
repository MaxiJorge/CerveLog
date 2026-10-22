package com.jorge.cervelog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme

@Composable
fun LoadingContent(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
fun MessageContent(
    emoji: String,
    titulo: String,
    descripcion: String,
    modifier: Modifier = Modifier,
    accion: String? = null,
    onAccion: () -> Unit = {},
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(AppTheme.spacing.xxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm, Alignment.CenterVertically),
    ) {
        Text(emoji, style = AppTheme.typography.displayMedium)
        Text(titulo, style = AppTheme.typography.titleLarge, textAlign = TextAlign.Center)
        Text(
            descripcion,
            style = AppTheme.typography.bodyMedium,
            color = AppTheme.colors.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        if (accion != null) {
            Button(onClick = onAccion, modifier = Modifier.padding(top = AppTheme.spacing.sm)) { Text(accion) }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MessageContentPreview() {
    CerveLogTheme {
        MessageContent(emoji = "🍻", titulo = "Todavía no hay cervezas", descripcion = "Pulsa + para añadir una.", accion = "Volver")
    }
}
