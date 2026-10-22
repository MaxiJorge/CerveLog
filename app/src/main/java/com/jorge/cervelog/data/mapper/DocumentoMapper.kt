package com.jorge.cervelog.data.mapper

import com.google.firebase.Timestamp
import com.jorge.cervelog.data.firestore.CervezaDocumento
import com.jorge.cervelog.data.firestore.PublicacionDocumento
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.Publicacion
import java.time.Instant

fun CervezaDocumento.toDomain() = Cerveza(
    id = id,
    nombre = nombre,
    cerveceria = cerveceria,
    estilo = estilo,
    abv = abv,
    ibu = ibu,
    puntuacion = puntuacion,
    notas = notas,
    lugar = lugar,
    ubicacion = if (latitud != null && longitud != null) Coordenadas(latitud, longitud) else null,
    foto = foto,
    fecha = fecha?.toInstant(),
)

fun Cerveza.toDocumento() = CervezaDocumento(
    id = id,
    nombre = nombre,
    cerveceria = cerveceria,
    estilo = estilo,
    abv = abv,
    ibu = ibu,
    puntuacion = puntuacion,
    notas = notas,
    lugar = lugar,
    latitud = ubicacion?.latitud,
    longitud = ubicacion?.longitud,
    foto = foto,
    fecha = fecha?.toTimestamp(),
)

fun PublicacionDocumento.toDomain() = Publicacion(
    id = id,
    autorUid = autorUid,
    autorEmail = autorEmail,
    nombre = nombre,
    cerveceria = cerveceria,
    estilo = estilo,
    abv = abv,
    ibu = ibu,
    puntuacion = puntuacion,
    lugar = lugar,
    ubicacion = Coordenadas(latitud, longitud),
    fecha = fecha?.toInstant(),
)

fun Publicacion.toDocumento() = PublicacionDocumento(
    id = id,
    autorUid = autorUid,
    autorEmail = autorEmail,
    nombre = nombre,
    cerveceria = cerveceria,
    estilo = estilo,
    abv = abv,
    ibu = ibu,
    puntuacion = puntuacion,
    lugar = lugar,
    latitud = ubicacion.latitud,
    longitud = ubicacion.longitud,
    fecha = fecha?.toTimestamp(),
)

fun Instant.toTimestamp() = Timestamp(this)
