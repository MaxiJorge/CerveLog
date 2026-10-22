package com.jorge.cervelog.ui

import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.testing.FakeCervezaRepository
import com.jorge.cervelog.domain.testing.MainDispatcherRule
import com.jorge.cervelog.domain.testing.cerveza
import com.jorge.cervelog.domain.usecase.EliminarCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObservarCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObservarCervezasUseCase
import com.jorge.cervelog.ui.cervezas.detail.CervezaDetailUiState
import com.jorge.cervelog.ui.cervezas.detail.CervezaDetailViewModel
import com.jorge.cervelog.ui.cervezas.list.CervezasUiState
import com.jorge.cervelog.ui.cervezas.list.CervezasViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CervezaViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val repo = FakeCervezaRepository(listOf(cerveza(id = "c1")))

    @Test
    fun `el listado pasa a Success con las cervezas`() = runTest {
        val viewModel = CervezasViewModel(ObservarCervezasUseCase(repo))
        // stateIn solo empieza a escuchar cuando alguien observa
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        val state = viewModel.uiState.value as CervezasUiState.Success
        assertEquals(listOf("c1"), state.cervezas.map { it.id })
    }

    @Test
    fun `el listado muestra el error del dominio`() = runTest {
        repo.error = DomainError.SinConexion
        val viewModel = CervezasViewModel(ObservarCervezasUseCase(repo))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }

        assertEquals(CervezasUiState.Error(DomainError.SinConexion), viewModel.uiState.value)
    }

    @Test
    fun `al eliminar se borra y se marca eliminada para volver atras`() = runTest {
        val viewModel = CervezaDetailViewModel("c1", ObservarCervezaUseCase(repo), EliminarCervezaUseCase(repo))
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { viewModel.uiState.collect() }
        assertTrue(viewModel.uiState.value is CervezaDetailUiState.Success)

        viewModel.onEliminar()

        assertTrue(viewModel.borrado.value.eliminada)
        assertTrue(repo.cervezas.value.isEmpty())
    }

    @Test
    fun `si falla el borrado queda el error para el snackbar`() = runTest {
        val viewModel = CervezaDetailViewModel("c1", ObservarCervezaUseCase(repo), EliminarCervezaUseCase(repo))
        repo.error = DomainError.SinPermiso

        viewModel.onEliminar()

        assertEquals(DomainError.SinPermiso, viewModel.borrado.value.error)
        viewModel.onErrorMostrado()
        assertEquals(null, viewModel.borrado.value.error)
    }
}
