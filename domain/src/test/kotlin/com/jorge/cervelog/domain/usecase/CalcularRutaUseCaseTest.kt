package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.ModoTransporte
import com.jorge.cervelog.domain.testing.FakeRutaRepository
import com.jorge.cervelog.domain.testing.PALERMO
import com.jorge.cervelog.domain.testing.RECOLETA
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CalcularRutaUseCaseTest {

    @Test
    fun `cerca se va a pie`() = runTest {
        val repo = FakeRutaRepository()
        val aSeisCientosMetros = Coordenadas(-34.5880, -58.4180)

        CalcularRutaUseCase(repo)(PALERMO, aSeisCientosMetros)

        assertEquals(ModoTransporte.A_PIE, repo.modoPedido)
    }

    @Test
    fun `lejos se va en coche`() = runTest {
        val repo = FakeRutaRepository()

        // Recoleta ya queda a unos 2,5 km
        CalcularRutaUseCase(repo)(PALERMO, RECOLETA)

        assertEquals(ModoTransporte.EN_COCHE, repo.modoPedido)
    }

    @Test
    fun `el error del repositorio llega tal cual`() = runTest {
        val repo = FakeRutaRepository(error = DomainError.RutaNoDisponible)

        val resultado = CalcularRutaUseCase(repo)(PALERMO, RECOLETA)

        assertEquals(AppResult.Error(DomainError.RutaNoDisponible), resultado)
    }
}
