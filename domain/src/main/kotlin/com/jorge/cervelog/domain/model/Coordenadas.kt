package com.jorge.cervelog.domain.model

import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

private const val RADIO_TIERRA_METROS = 6_371_000.0

data class Coordenadas(val latitud: Double, val longitud: Double) {

    // Fórmula del semiverseno
    fun distanciaMetrosHasta(otra: Coordenadas): Double {
        val dLat = Math.toRadians(otra.latitud - latitud)
        val dLng = Math.toRadians(otra.longitud - longitud)
        val a = sin(dLat / 2).pow(2) +
            cos(Math.toRadians(latitud)) * cos(Math.toRadians(otra.latitud)) * sin(dLng / 2).pow(2)
        return 2 * RADIO_TIERRA_METROS * asin(sqrt(a))
    }
}

// Rectángulo visible del mapa
data class Zona(val sur: Double, val oeste: Double, val norte: Double, val este: Double) {

    fun contiene(punto: Coordenadas): Boolean {
        if (punto.latitud !in sur..norte) return false
        // Si cruza el antimeridiano, el oeste es mayor que el este
        return if (oeste <= este) punto.longitud in oeste..este
        else punto.longitud >= oeste || punto.longitud <= este
    }
}
