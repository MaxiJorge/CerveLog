package com.jorge.cervelog.ui.cervezas.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.usecase.ObservarCervezasUseCase
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface CervezasUiState {
    data object Loading : CervezasUiState
    data class Success(val cervezas: List<Cerveza>) : CervezasUiState
    data class Error(val error: DomainError) : CervezasUiState
}

class CervezasViewModel(observarCervezas: ObservarCervezasUseCase) : ViewModel() {

    val uiState: StateFlow<CervezasUiState> = observarCervezas()
        .map { resultado ->
            when (resultado) {
                is AppResult.Success -> CervezasUiState.Success(resultado.data)
                is AppResult.Error -> CervezasUiState.Error(resultado.error)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), CervezasUiState.Loading)

    companion object {
        val Factory = viewModelFactory {
            initializer { CervezasViewModel(contenedor().observarCervezas) }
        }
    }
}
