package com.jorge.cervelog.domain.model

import java.time.Instant

data class Cerveza(
    val id: String = "",
    val nombre: String = "",
    val cerveceria: String = "",
    val estilo: String = "",
    val abv: Double? = null,
    val ibu: Int? = null,
    val puntuacion: Int = 0,
    val notas: String = "",
    val lugar: String = "",
    val ubicacion: Coordenadas? = null,
    // JPEG reducido en Base64
    val foto: String? = null,
    val fecha: Instant? = null,
)
