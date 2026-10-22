package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.testing.FakeAuthRepository
import com.jorge.cervelog.domain.testing.FakeCervezaRepository
import com.jorge.cervelog.domain.testing.cerveza
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class GuardarCervezaUseCaseTest {

    private val ahora = Instant.parse("2026-10-21T12:00:00Z")
    private val cervezas = FakeCervezaRepository()
    private val auth = FakeAuthRepository()
    private val guardar = GuardarCervezaUseCase(cervezas, auth, ahora = { ahora })

    @Test
    fun `recorta los textos y pone la fecha si es nueva`() = runTest {
        guardar(cerveza(id = "", nombre = "  Quilmes Stout  ", fecha = null))

        val guardada = cervezas.cervezas.value.single()
        assertEquals("Quilmes Stout", guardada.nombre)
        assertEquals(ahora, guardada.fecha)
    }

    @Test
    fun `al editar conserva la fecha original`() = runTest {
        val original = Instant.parse("2026-10-01T10:00:00Z")
        guardar(cerveza(fecha = original))

        assertEquals(original, cervezas.cervezas.value.single().fecha)
    }

    @Test
    fun `con ubicacion se publica en el mapa con el autor`() = runTest {
        guardar(cerveza(id = "c1"))

        val publicacion = cervezas.publicaciones.getValue("c1")
        assertEquals("uid-yo", publicacion.autorUid)
        assertEquals("yo@ejemplo.com", publicacion.autorEmail)
    }

    @Test
    fun `sin ubicacion no se publica`() = runTest {
        guardar(cerveza(id = "c1", ubicacion = null))

        assertTrue(cervezas.publicaciones.isEmpty())
    }

    @Test
    fun `valida nombre, puntuacion, ABV e IBU`() = runTest {
        assertError(DomainError.NombreVacio, guardar(cerveza(nombre = "   ")))
        assertError(DomainError.PuntuacionNoValida, guardar(cerveza(puntuacion = 0)))
        assertError(DomainError.AbvNoValido, guardar(cerveza().copy(abv = 120.0)))
        assertError(DomainError.IbuNoValido, guardar(cerveza().copy(ibu = -1)))
        assertTrue(cervezas.cervezas.value.isEmpty())
    }

    @Test
    fun `sin sesion no se guarda`() = runTest {
        val sinSesion = GuardarCervezaUseCase(cervezas, FakeAuthRepository(usuario = null))

        assertError(DomainError.SinSesion, sinSesion(cerveza()))
        assertNull(cervezas.cervezas.value.firstOrNull())
    }

    private fun assertError(esperado: DomainError, resultado: AppResult<*>) =
        assertEquals(AppResult.Error(esperado), resultado)
}
