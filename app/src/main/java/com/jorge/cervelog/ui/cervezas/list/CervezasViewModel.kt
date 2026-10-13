package com.jorge.cervelog.ui.cervezas.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorge.cervelog.data.model.Cerveza
import com.jorge.cervelog.data.repository.AuthRepository
import com.jorge.cervelog.data.repository.CervezaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

sealed interface CervezasUiState {
    data object Loading : CervezasUiState
    data class Success(val cervezas: List<Cerveza>) : CervezasUiState
    data class Error(val mensaje: String) : CervezasUiState
}

class CervezasViewModel(
    cervezaRepository: CervezaRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val emailUsuario: String = authRepository.currentUser?.email.orEmpty()

    val uiState: StateFlow<CervezasUiState> = cervezaRepository.observarCervezas()
        .map<List<Cerveza>, CervezasUiState> { CervezasUiState.Success(it) }
        .catch { emit(CervezasUiState.Error("No pudimos cargar tus cervezas")) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = CervezasUiState.Loading,
        )

    fun logout() = authRepository.logout()

    companion object {
        val Factory = viewModelFactory {
            initializer { CervezasViewModel(CervezaRepository(), AuthRepository()) }
        }
    }
}
