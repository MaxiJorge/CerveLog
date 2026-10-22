package com.jorge.cervelog.ui

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.testing.FakeAuthRepository
import com.jorge.cervelog.domain.testing.FakeCervezaRepository
import com.jorge.cervelog.domain.testing.FakeFotoRepository
import com.jorge.cervelog.domain.testing.FakeUbicacionRepository
import com.jorge.cervelog.domain.testing.MainDispatcherRule
import com.jorge.cervelog.domain.testing.PALERMO
import com.jorge.cervelog.domain.testing.RECOLETA
import com.jorge.cervelog.domain.testing.cerveza
import com.jorge.cervelog.domain.usecase.GuardarCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObtenerCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUbicacionActualUseCase
import com.jorge.cervelog.domain.usecase.ProcesarFotoUseCase
import com.jorge.cervelog.ui.cervezas.form.CervezaForm
import com.jorge.cervelog.ui.cervezas.form.CervezaFormViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

class CervezaFormViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeCervezaRepository(listOf(cerveza(id = "c1", nombre = "Patagonia")))
    private val ubicacion = FakeUbicacionRepository()

    private fun viewModel(id: String? = null) = CervezaFormViewModel(
        cervezaId = id,
        obtenerCerveza = ObtenerCervezaUseCase(repo),
        guardarCerveza = GuardarCervezaUseCase(repo, FakeAuthRepository()),
        obtenerUbicacionActual = ObtenerUbicacionActualUseCase(ubicacion),
        procesarFoto = ProcesarFotoUseCase(FakeFotoRepository()),
    )

    @Test
    fun `al editar carga el formulario con coma decimal`() {
        val vm = viewModel("c1")

        assertFalse(vm.uiState.value.cargando)
        assertEquals("Patagonia", vm.uiState.value.form.nombre)
        assertEquals("5,2", vm.uiState.value.form.abv)
    }

    @Test
    fun `ABV con texto no valido no se guarda`() {
        val vm = viewModel()
        vm.onFormChange(CervezaForm(nombre = "Quilmes", puntuacion = 3, abv = "fuerte"))

        vm.onGuardar()

        assertEquals(DomainError.AbvNoValido, vm.uiState.value.error)
        assertEquals(1, repo.cervezas.value.size)
    }

    @Test
    fun `la validacion del dominio llega al formulario`() {
        val vm = viewModel()
        vm.onFormChange(CervezaForm(nombre = "Quilmes", puntuacion = 0))

        vm.onGuardar()

        assertEquals(DomainError.PuntuacionNoValida, vm.uiState.value.error)
    }

    @Test
    fun `guardar una nueva acepta la coma y marca guardada`() {
        val vm = viewModel()
        vm.onFormChange(CervezaForm(nombre = "Quilmes Stout", puntuacion = 4, abv = "4,8"))

        vm.onGuardar()

        assertTrue(vm.uiState.value.guardada)
        assertEquals(4.8, repo.cervezas.value.last().abv!!, 0.0)
    }

    @Test
    fun `tocar el mapa fija la ubicacion y el GPS fallido avisa`() {
        val vm = viewModel()
        vm.onUbicacionElegida(RECOLETA)
        assertEquals(RECOLETA, vm.uiState.value.form.ubicacion)

        ubicacion.resultado = AppResult.Error(DomainError.UbicacionNoDisponible)
        vm.onUsarUbicacionActual()
        assertEquals(DomainError.UbicacionNoDisponible, vm.uiState.value.error)
        assertEquals(RECOLETA, vm.uiState.value.form.ubicacion)

        ubicacion.resultado = AppResult.Success(PALERMO)
        vm.onUsarUbicacionActual()
        assertEquals(PALERMO, vm.uiState.value.form.ubicacion)
    }
}
