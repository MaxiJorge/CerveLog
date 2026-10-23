package com.jorge.cervelog.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.ComposeMapColorScheme
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.ui.theme.AppTheme

private const val MARGEN_ENCUADRE_PX = 120

// El origen se ve con el punto azul de Google, así que solo hace falta el permiso
@Composable
fun MapaRuta(
    destino: Coordenadas,
    titulo: String,
    origen: Coordenadas?,
    recorrido: List<Coordenadas>,
    modifier: Modifier = Modifier,
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(destino.toLatLng(), ZOOM_CALLE)
    }
    var mapaCargado by remember { mutableStateOf(false) }

    // Encuadrar antes de que el mapa tenga tamaño lanza una excepción
    LaunchedEffect(mapaCargado, origen, recorrido) {
        val puntos = recorrido.ifEmpty { listOfNotNull(origen) } + destino
        if (!mapaCargado || puntos.size < 2) return@LaunchedEffect
        val limites = LatLngBounds.builder().apply { puntos.forEach { include(it.toLatLng()) } }.build()
        cameraPositionState.animate(CameraUpdateFactory.newLatLngBounds(limites, MARGEN_ENCUADRE_PX))
    }

    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .height(320.dp)
            .clip(RoundedCornerShape(12.dp)),
        cameraPositionState = cameraPositionState,
        mapColorScheme = if (AppTheme.oscuro) ComposeMapColorScheme.DARK else ComposeMapColorScheme.LIGHT,
        properties = MapProperties(isMyLocationEnabled = origen != null),
        // El botón de centrar tapaba el destino y el encuadre ya es automático
        uiSettings = MapUiSettings(myLocationButtonEnabled = false),
        onMapLoaded = { mapaCargado = true },
    ) {
        Marker(state = rememberUpdatedMarkerState(destino.toLatLng()), title = titulo)
        if (recorrido.isNotEmpty()) {
            Polyline(points = recorrido.map { it.toLatLng() }, color = AppTheme.colors.primary, width = 14f)
        }
    }
}
