package com.jorge.cervelog.ui.mapa.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.model.Ruta
import com.jorge.cervelog.domain.usecase.CalcularRutaUseCase
import com.jorge.cervelog.domain.usecase.ObtenerPublicacionUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUbicacionActualUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUsuarioActualUseCase
import com.jorge.cervelog.ui.navigation.DetallePublicacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

sealed interface PublicacionDetailUiState {
    data object Loading : PublicacionDetailUiState
    data class Success(val publicacion: Publicacion, val esMia: Boolean) : PublicacionDetailUiState
    data object NotFound : PublicacionDetailUiState
    data class Error(val error: DomainError) : PublicacionDetailUiState
}

data class RutaUiState(
    val origen: Coordenadas? = null,
    val ruta: Ruta? = null,
    val calculando: Boolean = false,
    val error: DomainError? = null,
)

class PublicacionDetailViewModel(
    publicacionId: String,
    obtenerPublicacion: ObtenerPublicacionUseCase,
    private val obtenerUbicacionActual: ObtenerUbicacionActualUseCase,
    private val calcularRuta: CalcularRutaUseCase,
    obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow<PublicacionDetailUiState>(PublicacionDetailUiState.Loading)
    val uiState: StateFlow<PublicacionDetailUiState> = _uiState.asStateFlow()

    private val _ruta = MutableStateFlow(RutaUiState())
    val ruta: StateFlow<RutaUiState> = _ruta.asStateFlow()

    // Solo se calcula una vez; el ViewModel sobrevive al giro de pantalla
    private var rutaPedida = false

    init {
        val uid = obtenerUsuarioActual()?.uid
        viewModelScope.launch {
            _uiState.value = when (val resultado = obtenerPublicacion(publicacionId)) {
                is AppResult.Success -> PublicacionDetailUiState.Success(resultado.data, resultado.data.autorUid == uid)
                is AppResult.Error ->
                    if (resultado.error == DomainError.NoEncontrada) PublicacionDetailUiState.NotFound
                    else PublicacionDetailUiState.Error(resultado.error)
            }
        }
    }

    // La pantalla la llama cuando ya tiene permiso de ubicación
    fun onCalcularRuta() {
        val destino = (_uiState.value as? PublicacionDetailUiState.Success)?.publicacion?.ubicacion ?: return
        if (rutaPedida) return
        rutaPedida = true
        viewModelScope.launch {
            _ruta.update { it.copy(calculando = true, error = null) }
            val origen = when (val resultado = obtenerUbicacionActual()) {
                is AppResult.Success -> resultado.data
                is AppResult.Error -> {
                    _ruta.update { it.copy(calculando = false, error = resultado.error) }
                    return@launch
                }
            }
            _ruta.update { it.copy(origen = origen) }
            _ruta.update {
                when (val resultado = calcularRuta(origen, destino)) {
                    is AppResult.Success -> it.copy(calculando = false, ruta = resultado.data)
                    is AppResult.Error -> it.copy(calculando = false, error = resultado.error)
                }
            }
        }
    }

    fun onErrorMostrado() = _ruta.update { it.copy(error = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                PublicacionDetailViewModel(
                    publicacionId = createSavedStateHandle().toRoute<DetallePublicacion>().publicacionId,
                    obtenerPublicacion = c.obtenerPublicacion,
                    obtenerUbicacionActual = c.obtenerUbicacionActual,
                    calcularRuta = c.calcularRuta,
                    obtenerUsuarioActual = c.obtenerUsuarioActual,
                )
            }
        }
    }
}
