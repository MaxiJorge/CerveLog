package com.jorge.cervelog.domain.model

import com.jorge.cervelog.domain.testing.publicacion
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class FiltroMapaTest {

    @Test
    fun `sin filtros entra todo`() {
        val filtro = FiltroMapa()

        assertEquals(0, filtro.activos)
        assertTrue(filtro.cumple(publicacion().copy(puntuacion = 1, abv = null)))
    }

    @Test
    fun `puntuacion minima`() {
        val filtro = FiltroMapa(puntuacionMinima = 4)

        assertTrue(filtro.cumple(publicacion().copy(puntuacion = 4)))
        assertFalse(filtro.cumple(publicacion().copy(puntuacion = 3)))
    }

    @Test
    fun `el estilo no distingue mayusculas ni espacios`() {
        val filtro = FiltroMapa(estilos = setOf("IPA", "Stout"))

        assertTrue(filtro.cumple(publicacion().copy(estilo = " ipa ")))
        assertFalse(filtro.cumple(publicacion().copy(estilo = "Lager")))
    }

    @Test
    fun `graduacion por tramos y sin ABV no entra`() {
        val filtro = FiltroMapa(graduaciones = setOf(Graduacion.MEDIA))

        assertTrue(filtro.cumple(publicacion().copy(abv = 5.0)))
        assertFalse(filtro.cumple(publicacion().copy(abv = 7.0)))
        assertFalse(filtro.cumple(publicacion().copy(abv = null)))
    }

    @Test
    fun `los filtros se combinan y se cuentan`() {
        val filtro = FiltroMapa(puntuacionMinima = 3, estilos = setOf("IPA"), graduaciones = setOf(Graduacion.FUERTE))

        assertEquals(3, filtro.activos)
        assertTrue(filtro.cumple(publicacion().copy(puntuacion = 5, estilo = "IPA", abv = 7.5)))
        assertFalse(filtro.cumple(publicacion().copy(puntuacion = 5, estilo = "IPA", abv = 6.0)))
    }

    @Test
    fun `estilos disponibles sin repetir y ordenados`() {
        val lista = listOf("Stout", "ipa", "IPA ", "", "Amber Lager").map { publicacion().copy(estilo = it) }

        assertEquals(listOf("Amber Lager", "ipa", "Stout"), lista.estilos())
    }
}
