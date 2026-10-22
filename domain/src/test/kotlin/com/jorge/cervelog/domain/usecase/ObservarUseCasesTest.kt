package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.Estadisticas
import com.jorge.cervelog.domain.model.Zona
import com.jorge.cervelog.domain.testing.CORDOBA
import com.jorge.cervelog.domain.testing.FakeAuthRepository
import com.jorge.cervelog.domain.testing.FakeCervezaRepository
import com.jorge.cervelog.domain.testing.FakePublicacionRepository
import com.jorge.cervelog.domain.testing.FakeUbicacionRepository
import com.jorge.cervelog.domain.testing.PALERMO
import com.jorge.cervelog.domain.testing.RECOLETA
import com.jorge.cervelog.domain.testing.cerveza
import com.jorge.cervelog.domain.testing.publicacion
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.Instant

class ObservarUseCasesTest {

    @Test
    fun `estadisticas del perfil`() = runTest {
        val repo = FakeCervezaRepository(
            listOf(cerveza(id = "1", puntuacion = 5), cerveza(id = "2", puntuacion = 2, ubicacion = null)),
        )

        val resultado = ObservarEstadisticasUseCase(repo)().first()

        assertEquals(AppResult.Success(Estadisticas(total = 2, enElMapa = 1, puntuacionMedia = 3.5)), resultado)
    }

    @Test
    fun `sin cervezas no hay nota media`() = runTest {
        val resultado = ObservarEstadisticasUseCase(FakeCervezaRepository())().first()

        assertEquals(AppResult.Success(Estadisticas()), resultado)
    }

    @Test
    fun `el error del repositorio llega a las estadisticas`() = runTest {
        val repo = FakeCervezaRepository().apply { error = DomainError.SinConexion }

        assertEquals(AppResult.Error(DomainError.SinConexion), ObservarEstadisticasUseCase(repo)().first())
    }

    @Test
    fun `solo avisa de las de otros a 5 km o menos`() = runTest {
        val publicaciones = FakePublicacionRepository()
        val avisos = ObservarCervezasCercanasUseCase(publicaciones, FakeUbicacionRepository(), FakeAuthRepository())

        publicaciones.nuevas.emit(publicacion(id = "mia", autorUid = "uid-yo", ubicacion = RECOLETA))
        publicaciones.nuevas.emit(publicacion(id = "lejos", ubicacion = CORDOBA))
        publicaciones.nuevas.emit(publicacion(id = "cerca", ubicacion = RECOLETA))

        val recibidas = avisos(Instant.EPOCH).take(1).toList()
        assertEquals("cerca", recibidas.single().publicacion.id)
        assertEquals(2_500.0, recibidas.single().distanciaMetros, 300.0)
    }

    @Test
    fun `distancia y zonas`() {
        assertEquals(0.0, PALERMO.distanciaMetrosHasta(PALERMO), 0.001)
        // De Buenos Aires a Córdoba hay unos 645 km
        assertEquals(645_000.0, PALERMO.distanciaMetrosHasta(CORDOBA), 10_000.0)

        val zona = Zona(sur = -35.0, oeste = -59.0, norte = -34.0, este = -58.0)
        assertTrue(zona.contiene(PALERMO))
        assertFalse(zona.contiene(CORDOBA))
        // Zona que cruza el antimeridiano
        assertTrue(Zona(sur = -10.0, oeste = 170.0, norte = 10.0, este = -170.0).contiene(Coordenadas(0.0, 179.0)))
    }
}
