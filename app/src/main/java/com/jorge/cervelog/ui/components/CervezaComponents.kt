package com.jorge.cervelog.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jorge.cervelog.R
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme

// Con onChange sirve para elegir la puntuación
@Composable
fun RatingStars(
    puntuacion: Int,
    modifier: Modifier = Modifier,
    size: Dp = 16.dp,
    onChange: ((Int) -> Unit)? = null,
    enabled: Boolean = true,
) {
    Row(modifier = modifier) {
        for (valor in 1..5) {
            val tint = if (valor <= puntuacion) AppTheme.colors.primary else AppTheme.colors.outlineVariant
            if (onChange == null) {
                Icon(Icons.Filled.Star, contentDescription = null, modifier = Modifier.size(size), tint = tint)
            } else {
                IconButton(onClick = { onChange(valor) }, enabled = enabled) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = stringResource(R.string.estrellas_de_5, valor),
                        modifier = Modifier.size(size),
                        tint = tint,
                    )
                }
            }
        }
    }
}

@Composable
fun AvatarCerveza(tamano: Dp, estilo: TextStyle, modifier: Modifier = Modifier) {
    Surface(shape = CircleShape, color = AppTheme.colors.secondaryContainer, modifier = modifier.size(tamano)) {
        Box(contentAlignment = Alignment.Center) {
            Text("🍺", style = estilo)
        }
    }
}

@Composable
fun BotonConCarga(texto: String, cargando: Boolean, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Button(
        onClick = onClick,
        enabled = !cargando,
        modifier = modifier
            .fillMaxWidth()
            .padding(top = AppTheme.spacing.sm),
    ) {
        if (cargando) {
            CircularProgressIndicator(
                modifier = Modifier.size(20.dp),
                strokeWidth = 2.dp,
                color = AppTheme.colors.onPrimary,
            )
        } else {
            Text(texto)
        }
    }
}

@Composable
fun FilaDato(etiqueta: String, valor: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(etiqueta, style = AppTheme.typography.bodyMedium, color = AppTheme.colors.onSurfaceVariant)
        Text(
            valor,
            style = AppTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.End,
            modifier = Modifier.padding(start = AppTheme.spacing.lg),
        )
    }
}

@Composable
fun TarjetaDatos(vararg filas: Pair<String, String>, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.surfaceContainer),
    ) {
        Column(Modifier.padding(AppTheme.spacing.lg)) {
            filas.forEachIndexed { i, (etiqueta, valor) ->
                if (i > 0) HorizontalDivider(Modifier.padding(vertical = AppTheme.spacing.sm))
                FilaDato(etiqueta, valor)
            }
        }
    }
}

@Composable
fun TarjetaCifra(etiqueta: String, valor: String, modifier: Modifier = Modifier) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = AppTheme.colors.secondaryContainer),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = AppTheme.spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(valor, style = AppTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(etiqueta, style = AppTheme.typography.labelMedium)
        }
    }
}

@Composable
fun DialogoConfirmacion(
    titulo: String,
    texto: String,
    textoConfirmar: String,
    onConfirmar: () -> Unit,
    onCancelar: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onCancelar,
        title = { Text(titulo) },
        text = { Text(texto) },
        confirmButton = { TextButton(onClick = onConfirmar) { Text(textoConfirmar) } },
        dismissButton = { TextButton(onClick = onCancelar) { Text(stringResource(R.string.cancelar)) } },
    )
}

@Preview(showBackground = true)
@Composable
private fun ComponentesPreview() {
    CerveLogTheme {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            RatingStars(3, size = 24.dp)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                TarjetaCifra("ABV", "5,2%", Modifier.weight(1f))
                TarjetaCifra("IBU", "35", Modifier.weight(1f))
            }
            TarjetaDatos("Estilo" to "IPA", "Lugar" to "Palermo")
            BotonConCarga("Guardar", cargando = false, onClick = {})
        }
    }
}
