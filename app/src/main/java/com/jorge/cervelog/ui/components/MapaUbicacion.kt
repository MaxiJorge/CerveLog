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
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.Zona

const val ZOOM_CIUDAD = 11f
const val ZOOM_BARRIO = 13f
private const val ZOOM_CALLE = 16f

val BUENOS_AIRES = LatLng(-34.6037, -58.3816)

fun Coordenadas.toLatLng() = LatLng(latitud, longitud)

fun LatLng.toCoordenadas() = Coordenadas(latitude, longitude)

fun LatLngBounds.toZona() = Zona(
    sur = southwest.latitude,
    oeste = southwest.longitude,
    norte = northeast.latitude,
    este = northeast.longitude,
)

@Composable
fun MapaUbicacion(
    ubicacion: Coordenadas?,
    titulo: String,
    modifier: Modifier = Modifier,
    onClick: ((Coordenadas) -> Unit)? = null,
) {
    val posicion = remember(ubicacion) { ubicacion?.toLatLng() }
    val cameraPositionState = rememberCameraPositionState {
        position = if (posicion != null) CameraPosition.fromLatLngZoom(posicion, ZOOM_CALLE)
        else CameraPosition.fromLatLngZoom(BUENOS_AIRES, ZOOM_CIUDAD)
    }
    // Si se veía toda la ciudad acerca a nivel de calle; si no, respeta el zoom
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
        onMapClick = onClick?.let { { latLng -> it(latLng.toCoordenadas()) } },
    ) {
        if (posicion != null) {
            Marker(state = rememberUpdatedMarkerState(posicion), title = titulo)
        }
    }
}
