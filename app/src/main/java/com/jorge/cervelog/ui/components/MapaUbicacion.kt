package com.jorge.cervelog.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState

private const val ZOOM_CALLE = 16f
private const val ZOOM_CIUDAD = 11f

// Sin ubicación todavía, el mapa empieza en Buenos Aires
private val POSICION_SIN_UBICACION = LatLng(-34.6037, -58.3816)

@Composable
fun MapaUbicacion(
    latitud: Double?,
    longitud: Double?,
    titulo: String,
    modifier: Modifier = Modifier,
    onClick: ((LatLng) -> Unit)? = null,
) {
    val posicion = remember(latitud, longitud) {
        if (latitud != null && longitud != null) LatLng(latitud, longitud) else null
    }
    val cameraPositionState = rememberCameraPositionState {
        position = if (posicion != null) CameraPosition.fromLatLngZoom(posicion, ZOOM_CALLE)
        else CameraPosition.fromLatLngZoom(POSICION_SIN_UBICACION, ZOOM_CIUDAD)
    }
    // Recentra la cámara cuando cambia la ubicación (GPS o toque en el mapa). Si se estaba
    // viendo toda la ciudad, acerca hasta nivel de calle; si no, respeta el zoom del usuario.
    LaunchedEffect(posicion) {
        if (posicion == null || cameraPositionState.position.target == posicion) return@LaunchedEffect
        val zoomActual = cameraPositionState.position.zoom
        val zoom = if (zoomActual <= ZOOM_CIUDAD) ZOOM_CALLE else zoomActual
        cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(posicion, zoom))
    }

    GoogleMap(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .clip(RoundedCornerShape(12.dp)),
        cameraPositionState = cameraPositionState,
        onMapClick = onClick,
    ) {
        if (posicion != null) {
            Marker(state = rememberUpdatedMarkerState(posicion), title = titulo)
        }
    }
}
