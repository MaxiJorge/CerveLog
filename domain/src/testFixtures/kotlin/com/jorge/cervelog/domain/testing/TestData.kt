package com.jorge.cervelog.domain.testing

import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.model.Usuario
import java.time.Instant

val PALERMO = Coordenadas(-34.5890, -58.4245)
val RECOLETA = Coordenadas(-34.5875, -58.3974)
val CORDOBA = Coordenadas(-31.4201, -64.1888)

fun usuario(uid: String = "uid-yo", email: String = "yo@ejemplo.com") = Usuario(uid, email)

fun cerveza(
    id: String = "c1",
    nombre: String = "Patagonia Amber Lager",
    puntuacion: Int = 4,
    ubicacion: Coordenadas? = PALERMO,
    fecha: Instant? = Instant.parse("2026-10-19T20:00:00Z"),
) = Cerveza(id = id, nombre = nombre, estilo = "Amber Lager", abv = 5.2, puntuacion = puntuacion, ubicacion = ubicacion, fecha = fecha)

fun publicacion(
    id: String = "p1",
    autorUid: String = "uid-ana",
    ubicacion: Coordenadas = RECOLETA,
) = Publicacion(id = id, autorUid = autorUid, autorEmail = "ana@aplicaciones.com", nombre = "Antares Kölsch", puntuacion = 4, ubicacion = ubicacion)
