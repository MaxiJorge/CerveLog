package com.jorge.cervelog.ui.mapa

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.jorge.cervelog.R
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.ui.components.RatingStars
import com.jorge.cervelog.ui.components.TarjetaDatos
import com.jorge.cervelog.ui.components.formatearAbv
import com.jorge.cervelog.ui.components.formatearFecha
import com.jorge.cervelog.ui.components.subtitulo
import com.jorge.cervelog.ui.theme.AppTheme
import com.jorge.cervelog.ui.theme.CerveLogTheme
import java.time.Instant

// Marcador del mapa con el nombre encima del pin
@Composable
fun EtiquetaMarcador(nombre: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Surface(
            shape = RoundedCornerShape(50),
            color = AppTheme.colors.primaryContainer,
            shadowElevation = 2.dp,
        ) {
            Text(
                nombre,
                style = AppTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .widthIn(max = 140.dp)
                    .padding(horizontal = AppTheme.spacing.sm, vertical = AppTheme.spacing.xs),
            )
        }
        Icon(
            Icons.Filled.Place,
            contentDescription = null,
            tint = AppTheme.colors.primary,
            modifier = Modifier.size(32.dp),
        )
    }
}

// Solo datos públicos: la foto y las notas son privadas
@Composable
fun DatosPublicacion(publicacion: Publicacion, esMia: Boolean, modifier: Modifier = Modifier) {
    val sinDato = stringResource(R.string.sin_dato)
    Column(modifier, verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.md)) {
        Text(publicacion.nombre, style = AppTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        val subtitulo = subtitulo(publicacion.cerveceria, publicacion.estilo)
        if (subtitulo.isNotEmpty()) {
            Text(subtitulo, style = AppTheme.typography.titleMedium, color = AppTheme.colors.onSurfaceVariant)
        }
        RatingStars(publicacion.puntuacion, size = 24.dp)

        TarjetaDatos(
            stringResource(R.string.abv) to (publicacion.abv?.formatearAbv() ?: sinDato),
            stringResource(R.string.ibu) to (publicacion.ibu?.toString() ?: sinDato),
            stringResource(R.string.lugar) to publicacion.lugar.ifBlank { sinDato },
            stringResource(R.string.fecha) to (publicacion.fecha?.formatearFecha() ?: sinDato),
            stringResource(R.string.registrada_por) to if (esMia) stringResource(R.string.ti) else publicacion.autorEmail,
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PublicacionComponentsPreview() {
    CerveLogTheme {
        Column(verticalArrangement = Arrangement.spacedBy(AppTheme.spacing.lg)) {
            EtiquetaMarcador("Antares Kölsch")
            DatosPublicacion(
                Publicacion(
                    nombre = "Antares Kölsch",
                    cerveceria = "Antares",
                    estilo = "Kölsch",
                    abv = 5.0,
                    puntuacion = 4,
                    lugar = "Recoleta",
                    autorEmail = "ana@aplicaciones.com",
                    fecha = Instant.now(),
                ),
                esMia = false,
            )
        }
    }
}
