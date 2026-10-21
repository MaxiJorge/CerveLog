package com.jorge.cervelog.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorge.cervelog.data.repository.AuthRepository
import com.jorge.cervelog.data.repository.CervezaRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

data class Estadisticas(
    val total: Int = 0,
    val enElMapa: Int = 0,
    val puntuacionMedia: Double? = null,
)

class PerfilViewModel(
    cervezaRepository: CervezaRepository,
    private val authRepository: AuthRepository,
) : ViewModel() {

    val emailUsuario: String = authRepository.currentUser?.email.orEmpty()

    // Reutiliza la misma consulta del listado, así no hace falta guardar contadores
    val estadisticas: StateFlow<Estadisticas?> = cervezaRepository.observarCervezas()
        .map<_, Estadisticas?> { cervezas ->
            Estadisticas(
                total = cervezas.size,
                enElMapa = cervezas.count { it.latitud != null },
                puntuacionMedia = cervezas.map { it.puntuacion }.average().takeIf { cervezas.isNotEmpty() },
            )
        }
        .catch { emit(null) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    fun logout() = authRepository.logout()

    companion object {
        val Factory = viewModelFactory {
            initializer { PerfilViewModel(CervezaRepository(), AuthRepository()) }
        }
    }
}
