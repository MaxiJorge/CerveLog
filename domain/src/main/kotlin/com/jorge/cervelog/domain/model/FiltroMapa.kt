package com.jorge.cervelog.domain.model

enum class Graduacion {
    SUAVE, MEDIA, FUERTE;

    companion object {
        fun de(abv: Double): Graduacion = when {
            abv < 5.0 -> SUAVE
            abv < 7.0 -> MEDIA
            else -> FUERTE
        }
    }
}

// Un conjunto vacío significa que ese filtro no se aplica
data class FiltroMapa(
    val puntuacionMinima: Int = 0,
    val estilos: Set<String> = emptySet(),
    val graduaciones: Set<Graduacion> = emptySet(),
) {
    val activos: Int
        get() = listOf(puntuacionMinima > 0, estilos.isNotEmpty(), graduaciones.isNotEmpty()).count { it }

    fun cumple(publicacion: Publicacion): Boolean {
        if (publicacion.puntuacion < puntuacionMinima) return false
        if (estilos.isNotEmpty() && estilos.none { it.equals(publicacion.estilo.trim(), ignoreCase = true) }) return false
        // Sin ABV no se sabe su graduación, así que no entra
        val abv = publicacion.abv
        if (graduaciones.isNotEmpty() && (abv == null || Graduacion.de(abv) !in graduaciones)) return false
        return true
    }
}

// El estilo se escribe a mano, así que "IPA" e "ipa " cuentan como uno
fun List<Publicacion>.estilos(): List<String> =
    map { it.estilo.trim() }
        .filter { it.isNotEmpty() }
        .distinctBy { it.lowercase() }
        .sortedBy { it.lowercase() }
