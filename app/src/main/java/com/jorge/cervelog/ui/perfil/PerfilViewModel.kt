package com.jorge.cervelog.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.Estadisticas
import com.jorge.cervelog.domain.usecase.CerrarSesionUseCase
import com.jorge.cervelog.domain.usecase.ComprobarVerificacionUseCase
import com.jorge.cervelog.domain.usecase.ObservarEstadisticasUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUsuarioActualUseCase
import com.jorge.cervelog.domain.usecase.ReenviarCorreoVerificacionUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PerfilUiState(
    val email: String = "",
    val emailVerificado: Boolean = true,
    val estadisticas: Estadisticas? = null,
    val enviandoCorreo: Boolean = false,
    val correoEnviado: Boolean = false,
    val error: DomainError? = null,
)

class PerfilViewModel(
    observarEstadisticas: ObservarEstadisticasUseCase,
    obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
    private val comprobarVerificacion: ComprobarVerificacionUseCase,
    private val reenviarCorreo: ReenviarCorreoVerificacionUseCase,
    private val cerrarSesion: CerrarSesionUseCase,
) : ViewModel() {

    private val usuario = obtenerUsuarioActual()

    private val cuenta = MutableStateFlow(
        PerfilUiState(email = usuario?.email.orEmpty(), emailVerificado = usuario?.emailVerificado ?: true),
    )

    val uiState: StateFlow<PerfilUiState> = combine(cuenta, observarEstadisticas()) { estado, estadisticas ->
        estado.copy(estadisticas = (estadisticas as? AppResult.Success)?.data)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), cuenta.value)

    init {
        // El usuario guardado no se entera de que ha pulsado el enlace hasta que se recarga
        viewModelScope.launch {
            val resultado = comprobarVerificacion()
            if (resultado is AppResult.Success) cuenta.update { it.copy(emailVerificado = resultado.data.emailVerificado) }
        }
    }

    fun onReenviarCorreo() {
        if (cuenta.value.enviandoCorreo) return
        viewModelScope.launch {
            cuenta.update { it.copy(enviandoCorreo = true, error = null) }
            val resultado = reenviarCorreo()
            cuenta.update {
                when (resultado) {
                    is AppResult.Success -> it.copy(enviandoCorreo = false, correoEnviado = true)
                    is AppResult.Error -> it.copy(enviandoCorreo = false, error = resultado.error)
                }
            }
        }
    }

    fun onErrorMostrado() = cuenta.update { it.copy(error = null) }

    fun onCerrarSesion() = cerrarSesion()

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                PerfilViewModel(
                    c.observarEstadisticas,
                    c.obtenerUsuarioActual,
                    c.comprobarVerificacion,
                    c.reenviarCorreoVerificacion,
                    c.cerrarSesion,
                )
            }
        }
    }
}
