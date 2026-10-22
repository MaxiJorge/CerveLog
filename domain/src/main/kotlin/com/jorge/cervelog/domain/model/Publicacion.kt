package com.jorge.cervelog.domain.model

import java.time.Instant

// Copia pública para el mapa, sin foto ni notas
data class Publicacion(
    val id: String = "",
    val autorUid: String = "",
    val autorEmail: String = "",
    val nombre: String = "",
    val cerveceria: String = "",
    val estilo: String = "",
    val abv: Double? = null,
    val ibu: Int? = null,
    val puntuacion: Int = 0,
    val lugar: String = "",
    val ubicacion: Coordenadas = Coordenadas(0.0, 0.0),
    val fecha: Instant? = null,
)

fun Cerveza.toPublicacion(autor: Usuario, ubicacion: Coordenadas) = Publicacion(
    id = id,
    autorUid = autor.uid,
    autorEmail = autor.email,
    nombre = nombre,
    cerveceria = cerveceria,
    estilo = estilo,
    abv = abv,
    ibu = ibu,
    puntuacion = puntuacion,
    lugar = lugar,
    ubicacion = ubicacion,
    fecha = fecha,
)

data class CervezaCercana(val publicacion: Publicacion, val distanciaMetros: Double)
