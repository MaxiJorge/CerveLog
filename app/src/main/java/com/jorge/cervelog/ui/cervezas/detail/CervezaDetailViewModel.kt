package com.jorge.cervelog.ui.cervezas.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorge.cervelog.data.model.Cerveza
import com.jorge.cervelog.data.repository.CervezaRepository
import com.jorge.cervelog.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface CervezaDetailUiState {
    data object Loading : CervezaDetailUiState
    data class Success(val cerveza: Cerveza) : CervezaDetailUiState
    data object NotFound : CervezaDetailUiState
    data object Error : CervezaDetailUiState
}

data class BorradoUiState(
    val eliminando: Boolean = false,
    val eliminada: Boolean = false,
    val error: String? = null,
)

class CervezaDetailViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CervezaRepository,
) : ViewModel() {

    private val cervezaId: String = checkNotNull(savedStateHandle[Routes.ARG_CERVEZA_ID])

    val uiState: StateFlow<CervezaDetailUiState> = repository.observarCerveza(cervezaId)
        .map { if (it != null) CervezaDetailUiState.Success(it) else CervezaDetailUiState.NotFound }
        .catch { emit(CervezaDetailUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CervezaDetailUiState.Loading)

    private val _borrado = MutableStateFlow(BorradoUiState())
    val borrado: StateFlow<BorradoUiState> = _borrado.asStateFlow()

    fun eliminar() {
        viewModelScope.launch {
            _borrado.update { it.copy(eliminando = true, error = null) }
            try {
                repository.eliminarCerveza(cervezaId)
                _borrado.update { it.copy(eliminando = false, eliminada = true) }
            } catch (e: Exception) {
                _borrado.update { it.copy(eliminando = false, error = "No pudimos eliminar la cerveza") }
            }
        }
    }

    fun errorMostrado() = _borrado.update { it.copy(error = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer { CervezaDetailViewModel(createSavedStateHandle(), CervezaRepository()) }
        }
    }
}
