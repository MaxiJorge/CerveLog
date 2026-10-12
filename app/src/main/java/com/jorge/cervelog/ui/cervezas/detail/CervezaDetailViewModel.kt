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
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface CervezaDetailUiState {
    data object Loading : CervezaDetailUiState
    data class Success(val cerveza: Cerveza) : CervezaDetailUiState
    data object NotFound : CervezaDetailUiState
    data object Error : CervezaDetailUiState
}

class CervezaDetailViewModel(
    savedStateHandle: SavedStateHandle,
    repository: CervezaRepository,
) : ViewModel() {

    // El id llega como argumento de la ruta de navegación.
    private val cervezaId: String = checkNotNull(savedStateHandle[Routes.ARG_CERVEZA_ID])

    val uiState: StateFlow<CervezaDetailUiState> = repository.observarCerveza(cervezaId)
        .map { if (it != null) CervezaDetailUiState.Success(it) else CervezaDetailUiState.NotFound }
        .catch { emit(CervezaDetailUiState.Error) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CervezaDetailUiState.Loading)

    companion object {
        val Factory = viewModelFactory {
            initializer { CervezaDetailViewModel(createSavedStateHandle(), CervezaRepository()) }
        }
    }
}
