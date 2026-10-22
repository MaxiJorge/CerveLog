package com.jorge.cervelog.ui.cervezas.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.usecase.EliminarCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObservarCervezaUseCase
import com.jorge.cervelog.ui.navigation.DetalleCerveza
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CervezaDetailUiState {
    data object Loading : CervezaDetailUiState
    data class Success(val cerveza: Cerveza) : CervezaDetailUiState
    data object NotFound : CervezaDetailUiState
    data class Error(val error: DomainError) : CervezaDetailUiState
}

data class BorradoUiState(
    val eliminando: Boolean = false,
    val eliminada: Boolean = false,
    val error: DomainError? = null,
)

class CervezaDetailViewModel(
    private val cervezaId: String,
    observarCerveza: ObservarCervezaUseCase,
    private val eliminarCerveza: EliminarCervezaUseCase,
) : ViewModel() {

    val uiState: StateFlow<CervezaDetailUiState> = observarCerveza(cervezaId)
        .map { resultado ->
            when (resultado) {
                is AppResult.Success -> CervezaDetailUiState.Success(resultado.data)
                is AppResult.Error ->
                    if (resultado.error == DomainError.NoEncontrada) CervezaDetailUiState.NotFound
                    else CervezaDetailUiState.Error(resultado.error)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CervezaDetailUiState.Loading)

    private val _borrado = MutableStateFlow(BorradoUiState())
    val borrado: StateFlow<BorradoUiState> = _borrado.asStateFlow()

    fun onEliminar() {
        if (_borrado.value.eliminando) return
        viewModelScope.launch {
            _borrado.update { it.copy(eliminando = true, error = null) }
            _borrado.update {
                when (val resultado = eliminarCerveza(cervezaId)) {
                    is AppResult.Success -> it.copy(eliminando = false, eliminada = true)
                    is AppResult.Error -> it.copy(eliminando = false, error = resultado.error)
                }
            }
        }
    }

    fun onErrorMostrado() = _borrado.update { it.copy(error = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                CervezaDetailViewModel(
                    cervezaId = createSavedStateHandle().toRoute<DetalleCerveza>().cervezaId,
                    observarCerveza = c.observarCerveza,
                    eliminarCerveza = c.eliminarCerveza,
                )
            }
        }
    }
}
