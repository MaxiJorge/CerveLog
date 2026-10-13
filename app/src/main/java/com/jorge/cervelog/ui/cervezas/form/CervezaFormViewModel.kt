package com.jorge.cervelog.ui.cervezas.form

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.google.firebase.Timestamp
import com.jorge.cervelog.data.model.Cerveza
import com.jorge.cervelog.data.repository.CervezaRepository
import com.jorge.cervelog.data.repository.UbicacionRepository
import com.jorge.cervelog.ui.navigation.Routes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// ABV e IBU se guardan como texto hasta validarlos
data class CervezaForm(
    val nombre: String = "",
    val cerveceria: String = "",
    val estilo: String = "",
    val abv: String = "",
    val ibu: String = "",
    val puntuacion: Int = 0,
    val lugar: String = "",
    val notas: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
)

data class CervezaFormUiState(
    val form: CervezaForm = CervezaForm(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val obteniendoUbicacion: Boolean = false,
    val error: String? = null,
    val guardada: Boolean = false,
)

class CervezaFormViewModel(
    savedStateHandle: SavedStateHandle,
    private val repository: CervezaRepository,
    private val ubicacionRepository: UbicacionRepository,
) : ViewModel() {

    private val cervezaId: String? = savedStateHandle[Routes.ARG_CERVEZA_ID]
    val esEdicion: Boolean = cervezaId != null

    // Al editar se conservan los campos que no están en el formulario (fecha, foto)
    private var original: Cerveza? = null

    private val _uiState = MutableStateFlow(CervezaFormUiState(cargando = esEdicion))
    val uiState: StateFlow<CervezaFormUiState> = _uiState.asStateFlow()

    init {
        if (cervezaId != null) cargar(cervezaId)
    }

    private fun cargar(id: String) {
        viewModelScope.launch {
            try {
                val cerveza = repository.obtenerCerveza(id)
                if (cerveza == null) {
                    _uiState.update { it.copy(cargando = false, error = "Esta cerveza ya no existe") }
                    return@launch
                }
                original = cerveza
                _uiState.update { it.copy(cargando = false, form = cerveza.toForm()) }
            } catch (e: Exception) {
                _uiState.update { it.copy(cargando = false, error = "No pudimos cargar la cerveza") }
            }
        }
    }

    fun onFormChange(form: CervezaForm) = _uiState.update { it.copy(form = form) }

    fun errorMostrado() = _uiState.update { it.copy(error = null) }

    fun usarUbicacionActual() {
        viewModelScope.launch {
            _uiState.update { it.copy(obteniendoUbicacion = true, error = null) }
            val ubicacion = try {
                ubicacionRepository.obtenerUbicacionActual()
            } catch (e: Exception) {
                null
            }
            _uiState.update {
                if (ubicacion == null) {
                    it.copy(
                        obteniendoUbicacion = false,
                        error = "No pudimos obtener tu ubicación. Comprueba que esté activada.",
                    )
                } else {
                    it.copy(
                        obteniendoUbicacion = false,
                        form = it.form.copy(latitud = ubicacion.latitude, longitud = ubicacion.longitude),
                    )
                }
            }
        }
    }

    fun permisoUbicacionDenegado() =
        _uiState.update { it.copy(error = "Sin permiso de ubicación no podemos registrar dónde la probaste") }

    fun guardar() {
        val form = _uiState.value.form
        val abv = form.abv.replace(',', '.').toDoubleOrNull()
        val ibu = form.ibu.toIntOrNull()
        val errorValidacion = when {
            // Si falló la carga, guardar crearía una nueva en vez de editar
            esEdicion && original == null -> "No pudimos cargar la cerveza a editar"
            form.nombre.isBlank() -> "Introduce el nombre de la cerveza"
            form.puntuacion !in 1..5 -> "Elige una puntuación de 1 a 5 estrellas"
            form.abv.isNotBlank() && (abv == null || abv !in 0.0..100.0) -> "El ABV tiene que ser un número entre 0 y 100"
            form.ibu.isNotBlank() && (ibu == null || ibu !in 0..1000) -> "El IBU tiene que ser un número entero entre 0 y 1000"
            else -> null
        }
        if (errorValidacion != null) {
            _uiState.update { it.copy(error = errorValidacion) }
            return
        }

        val base = original ?: Cerveza()
        val cerveza = base.copy(
            nombre = form.nombre.trim(),
            cerveceria = form.cerveceria.trim(),
            estilo = form.estilo.trim(),
            abv = abv,
            ibu = ibu,
            puntuacion = form.puntuacion,
            lugar = form.lugar.trim(),
            notas = form.notas.trim(),
            latitud = form.latitud,
            longitud = form.longitud,
            // El listado ordena por fecha
            fecha = base.fecha ?: Timestamp.now(),
        )

        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            try {
                repository.guardarCerveza(cerveza)
                _uiState.update { it.copy(guardando = false, guardada = true) }
            } catch (e: Exception) {
                _uiState.update { it.copy(guardando = false, error = "No pudimos guardar la cerveza") }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                CervezaFormViewModel(
                    createSavedStateHandle(),
                    CervezaRepository(),
                    UbicacionRepository(checkNotNull(this[APPLICATION_KEY])),
                )
            }
        }
    }
}

private fun Cerveza.toForm() = CervezaForm(
    nombre = nombre,
    cerveceria = cerveceria,
    estilo = estilo,
    // Decimal con coma; al guardar se acepta coma o punto
    abv = abv?.toString()?.removeSuffix(".0")?.replace('.', ',').orEmpty(),
    ibu = ibu?.toString().orEmpty(),
    puntuacion = puntuacion,
    lugar = lugar,
    notas = notas,
    latitud = latitud,
    longitud = longitud,
)
