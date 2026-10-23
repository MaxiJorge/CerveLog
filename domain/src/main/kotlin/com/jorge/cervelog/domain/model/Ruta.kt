package com.jorge.cervelog.domain.model

import java.time.Duration

enum class ModoTransporte { A_PIE, EN_COCHE }

// Recorrido por calles desde el origen hasta el destino
data class Ruta(
    val puntos: List<Coordenadas>,
    val distanciaMetros: Int,
    val duracion: Duration,
    val modo: ModoTransporte,
)
