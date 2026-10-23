package com.jorge.cervelog.ui.mapa

import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.FiltroMapa
import com.jorge.cervelog.domain.model.Graduacion
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme

private val PUNTUACIONES = listOf(
    0 to R.string.filtro_todas,
    3 to R.string.filtro_3_o_mas,
    4 to R.string.filtro_4_o_mas,
    5 to R.string.filtro_5,
)

@get:StringRes
private val Graduacion.texto: Int
    get() = when (this) {
        Graduacion.SUAVE -> R.string.graduacion_suave
        Graduacion.MEDIA -> R.string.graduacion_media
        Graduacion.FUERTE -> R.string.graduacion_fuerte
    }

// Botón de la barra con el número de filtros activos
@Composable
fun BotonFiltros(activos: Int, onClick: () -> Unit) {
    IconButton(onClick = onClick) {
        BadgedBox(badge = { if (activos > 0) Badge { Text(activos.toString()) } }) {
            Icon(painterResource(R.drawable.ic_filtro), contentDescription = stringResource(R.string.filtros))
        }
    }
}

// Cada cambio se aplica al momento: el mapa se actualiza detrás de la hoja
@Composable
fun FiltrosMapa(
    filtro: FiltroMapa,
    estilosDisponibles: List<String>,
    resultados: Int,
    onFiltroCambiado: (FiltroMapa) -> Unit,
    onCerrar: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(start = AppTheme.spacing.xl, end = AppTheme.spacing.xl, bottom = AppTheme.spacing.xxl),
        verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                stringResource(R.string.filtros),
                style = AppTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f),
            )
            TextButton(onClick = { onFiltroCambiado(FiltroMapa()) }, enabled = filtro.activos > 0) {
                Text(stringResource(R.string.limpiar))
            }
        }

        Apartado(stringResource(R.string.puntuacion_minima)) {
            PUNTUACIONES.forEach { (valor, texto) ->
                FilterChip(
                    selected = filtro.puntuacionMinima == valor,
                    onClick = { onFiltroCambiado(filtro.copy(puntuacionMinima = valor)) },
                    label = { Text(stringResource(texto)) },
                )
            }
        }

        Apartado(stringResource(R.string.estilo)) {
            if (estilosDisponibles.isEmpty()) {
                Text(
                    stringResource(R.string.sin_estilos),
                    style = AppTheme.typography.bodyMedium,
                    color = AppTheme.colors.onSurfaceVariant,
                )
            }
            estilosDisponibles.forEach { estilo ->
                val elegido = filtro.estilos.any { it.equals(estilo, ignoreCase = true) }
                FilterChip(
                    selected = elegido,
                    onClick = {
                        val estilos = if (elegido) filtro.estilos.filterNot { it.equals(estilo, ignoreCase = true) }.toSet()
                        else filtro.estilos + estilo
                        onFiltroCambiado(filtro.copy(estilos = estilos))
                    },
                    label = { Text(estilo) },
                )
            }
        }

        Apartado(stringResource(R.string.graduacion)) {
            Graduacion.entries.forEach { graduacion ->
                val elegida = graduacion in filtro.graduaciones
                FilterChip(
                    selected = elegida,
                    onClick = {
                        val graduaciones = if (elegida) filtro.graduaciones - graduacion else filtro.graduaciones + graduacion
                        onFiltroCambiado(filtro.copy(graduaciones = graduaciones))
                    },
                    label = { Text(stringResource(graduacion.texto)) },
                )
            }
        }

        Button(onClick = onCerrar, modifier = Modifier.fillMaxWidth()) {
            Text(pluralStringResource(R.plurals.ver_cervezas, resultados, resultados))
        }
    }
}

@Composable
private fun Apartado(titulo: String, chips: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.xs)) {
        Text(titulo, style = AppTheme.typography.titleSmall, color = AppTheme.colors.primary)
        FlowRow(horizontalArrangement = Arrangement.spacedBy(AppTheme.spacing.sm)) { chips() }
    }
}

@Preview(showBackground = true)
@Composable
private fun FiltrosMapaPreview() {
    CerveLogTheme {
        FiltrosMapa(
            filtro = FiltroMapa(puntuacionMinima = 4, estilos = setOf("IPA")),
            estilosDisponibles = listOf("Amber Lager", "IPA", "Kölsch", "Stout"),
            resultados = 3,
            onFiltroCambiado = {},
            onCerrar = {},
        )
    }
}
