package com.jorge.cervelog.ui.mapa

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.jorge.cervelog.data.model.Publicacion
import com.jorge.cervelog.data.repository.AuthRepository
import com.jorge.cervelog.data.repository.PublicacionRepository
import com.jorge.cervelog.data.repository.UbicacionRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class MapaComunitarioUiState(
    val publicaciones: List<Publicacion> = emptyList(),
    val cargando: Boolean = false,
    val error: String? = null,
)

class MapaComunitarioViewModel(
    private val repository: PublicacionRepository,
    private val ubicacionRepository: UbicacionRepository,
    authRepository: AuthRepository,
) : ViewModel() {

    val uidUsuario: String = authRepository.currentUser?.uid.orEmpty()

    private val _uiState = MutableStateFlow(MapaComunitarioUiState())
    val uiState: StateFlow<MapaComunitarioUiState> = _uiState.asStateFlow()

    private var busqueda: Job? = null

    // Se acumula lo ya descargado: si al mover el mapa no llega nada nuevo,
    // la lista no cambia y los marcadores no se vuelven a dibujar
    private val descargadas = LinkedHashMap<String, Publicacion>()

    // El ViewModel sobrevive al cambiar de pestaña, así que solo se centra la primera vez
    private val _miUbicacion = MutableStateFlow<LatLng?>(null)
    val miUbicacion: StateFlow<LatLng?> = _miUbicacion.asStateFlow()
    private var ubicacionPedida = false

    fun centrarEnMiUbicacion() {
        if (ubicacionPedida) return
        ubicacionPedida = true
        viewModelScope.launch {
            val ubicacion = try {
                ubicacionRepository.obtenerUbicacionActual()
            } catch (e: Exception) {
                null
            }
            if (ubicacion == null) {
                _uiState.update { it.copy(error = "No pudimos obtener tu ubicación") }
            } else {
                _miUbicacion.value = LatLng(ubicacion.latitude, ubicacion.longitude)
            }
        }
    }

    // Se llama cada vez que el mapa deja de moverse
    fun cargarZona(zona: LatLngBounds) {
        busqueda?.cancel()
        busqueda = viewModelScope.launch {
            _uiState.update { it.copy(cargando = true) }
            try {
                val enZona = repository.buscarEnZona(zona).associateBy { it.id }
                // Las que estaban en esta zona y ya no llegan se han borrado
                descargadas.entries.removeAll { (id, p) ->
                    id !in enZona && zona.contains(LatLng(p.latitud, p.longitud))
                }
                descargadas.putAll(enZona)
                _uiState.update { it.copy(cargando = false, publicaciones = descargadas.values.toList()) }
            } catch (e: CancellationException) {
                // La búsqueda anterior se cancela al mover otra vez el mapa
                throw e
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No pudimos cargar las cervezas del mapa") }
            }
        }
    }

    fun errorMostrado() = _uiState.update { it.copy(error = null) }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                MapaComunitarioViewModel(
                    PublicacionRepository(),
                    UbicacionRepository(checkNotNull(this[APPLICATION_KEY])),
                    AuthRepository(),
                )
            }
        }
    }
}
