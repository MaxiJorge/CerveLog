package com.jorge.cervelog.ui.mapa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.FiltroMapa
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.model.Zona
import com.jorge.cervelog.domain.model.estilos
import com.jorge.cervelog.domain.usecase.BuscarPublicacionesEnZonaUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUbicacionActualUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUsuarioActualUseCase
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapaComunitarioUiState(
    val publicaciones: List<Publicacion> = emptyList(),
    val filtro: FiltroMapa = FiltroMapa(),
    val cargando: Boolean = false,
    val error: DomainError? = null,
) {
    // Se filtra lo ya descargado, sin volver a consultar Firestore
    val visibles: List<Publicacion> get() = publicaciones.filter(filtro::cumple)

    // Los elegidos se mantienen aunque ya no haya ninguno en la zona
    val estilosDisponibles: List<String> get() = (publicaciones.estilos() + filtro.estilos).distinctBy { it.lowercase() }
}

class MapaComunitarioViewModel(
    private val buscarEnZona: BuscarPublicacionesEnZonaUseCase,
    private val obtenerUbicacionActual: ObtenerUbicacionActualUseCase,
    obtenerUsuarioActual: ObtenerUsuarioActualUseCase,
) : ViewModel() {

    val uidUsuario: String = obtenerUsuarioActual()?.uid.orEmpty()

    private val _uiState = MutableStateFlow(MapaComunitarioUiState())
    val uiState: StateFlow<MapaComunitarioUiState> = _uiState.asStateFlow()

    private var busqueda: Job? = null

    // Se acumula lo descargado para no redibujar los marcadores si no llega nada nuevo
    private val descargadas = LinkedHashMap<String, Publicacion>()

    // Solo se centra la primera vez, el ViewModel sobrevive al cambiar de pestaña
    private val _miUbicacion = MutableStateFlow<Coordenadas?>(null)
    val miUbicacion: StateFlow<Coordenadas?> = _miUbicacion.asStateFlow()
    private var ubicacionPedida = false

    fun onCentrarEnMiUbicacion() {
        if (ubicacionPedida) return
        ubicacionPedida = true
        viewModelScope.launch {
            when (val resultado = obtenerUbicacionActual()) {
                is AppResult.Success -> _miUbicacion.value = resultado.data
                is AppResult.Error -> _uiState.update { it.copy(error = resultado.error) }
            }
        }
    }

    fun onZonaVisible(zona: Zona) {
        busqueda?.cancel()
        busqueda = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            when (val resultado = buscarEnZona(zona)) {
                is AppResult.Success -> {
                    val enZona = resultado.data.associateBy { it.id }
                    // Las que estaban en la zona y ya no llegan se han borrado
                    descargadas.entries.removeAll { (id, p) -> id !in enZona && zona.contiene(p.ubicacion) }
                    descargadas.putAll(enZona)
                    _uiState.update { it.copy(cargando = false, publicaciones = descargadas.values.toList()) }
                }
                is AppResult.Error -> _uiState.update { it.copy(cargando = false, error = resultado.error) }
            }
        }
    }

    fun onFiltroCambiado(filtro: FiltroMapa) = _uiState.update { it.copy(filtro = filtro) }

    fun onErrorMostrado() = _uiState.update { it.copy(error = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                MapaComunitarioViewModel(c.buscarPublicacionesEnZona, c.obtenerUbicacionActual, c.obtenerUsuarioActual)
            }
        }
    }
}
