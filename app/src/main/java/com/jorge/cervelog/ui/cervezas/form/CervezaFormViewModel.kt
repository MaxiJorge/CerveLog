package com.jorge.cervelog.ui.cervezas.form

import androidx.lifecycle.ViewModel
import androidx.lifecycle.createSavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.toRoute
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.usecase.GuardarCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObtenerCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUbicacionActualUseCase
import com.jorge.cervelog.domain.usecase.ProcesarFotoUseCase
import com.jorge.cervelog.ui.navigation.FormularioCerveza
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

// Sin id crea y con id edita
class CervezaFormViewModel(
    private val cervezaId: String?,
    private val obtenerCerveza: ObtenerCervezaUseCase,
    private val guardarCerveza: GuardarCervezaUseCase,
    private val obtenerUbicacionActual: ObtenerUbicacionActualUseCase,
    private val procesarFoto: ProcesarFotoUseCase,
) : ViewModel() {

    val esEdicion: Boolean = cervezaId != null

    // Al editar conserva el id y la fecha
    private var original: Cerveza? = null

    private val _uiState = MutableStateFlow(CervezaFormUiState(cargando = esEdicion))
    val uiState: StateFlow<CervezaFormUiState> = _uiState.asStateFlow()

    private var lecturaUbicacion: Job? = null

    init {
        if (cervezaId != null) cargar(cervezaId)
    }

    private fun cargar(id: String) {
        viewModelScope.launch {
            when (val resultado = obtenerCerveza(id)) {
                is AppResult.Success -> {
                    original = resultado.data
                    _uiState.update { it.copy(cargando = false, form = resultado.data.toForm()) }
                }
                is AppResult.Error -> _uiState.update { it.copy(cargando = false, error = resultado.error) }
            }
        }
    }

    fun onFormChange(form: CervezaForm) = _uiState.update { it.copy(form = form) }

    fun onErrorMostrado() = _uiState.update { it.copy(error = null) }

    fun onUsarUbicacionActual() {
        lecturaUbicacion?.cancel()
        lecturaUbicacion = viewModelScope.launch {
            _uiState.update { it.copy(obteniendoUbicacion = true, error = null) }
            val resultado = obtenerUbicacionActual()
            _uiState.update {
                when (resultado) {
                    is AppResult.Success -> it.copy(obteniendoUbicacion = false, form = it.form.copy(ubicacion = resultado.data))
                    is AppResult.Error -> it.copy(obteniendoUbicacion = false, error = resultado.error)
                }
            }
        }
    }

    // Cancela la lectura del GPS para que no pise el punto elegido
    fun onUbicacionElegida(ubicacion: Coordenadas) {
        lecturaUbicacion?.cancel()
        _uiState.update { it.copy(obteniendoUbicacion = false, form = it.form.copy(ubicacion = ubicacion)) }
    }

    fun onPermisoUbicacionDenegado() = _uiState.update { it.copy(error = DomainError.SinPermisoUbicacion) }

    fun onFotoElegida(uri: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(procesandoFoto = true, error = null) }
            val resultado = procesarFoto(uri)
            _uiState.update {
                when (resultado) {
                    is AppResult.Success -> it.copy(procesandoFoto = false, form = it.form.copy(foto = resultado.data))
                    is AppResult.Error -> it.copy(procesandoFoto = false, error = resultado.error)
                }
            }
        }
    }

    fun onCamaraNoDisponible() = _uiState.update { it.copy(error = DomainError.CamaraNoDisponible) }

    fun onGuardar() {
        val state = _uiState.value
        if (state.guardando) return
        val form = state.form
        val abv = form.abv.replace(',', '.').toDoubleOrNull()
        val ibu = form.ibu.toIntOrNull()
        val errorFormato = when {
            // Si falló la carga, guardar crearía una nueva
            esEdicion && original == null -> DomainError.NoEncontrada
            form.abv.isNotBlank() && abv == null -> DomainError.AbvNoValido
            form.ibu.isNotBlank() && ibu == null -> DomainError.IbuNoValido
            else -> null
        }
        if (errorFormato != null) {
            _uiState.update { it.copy(error = errorFormato) }
            return
        }

        val cerveza = (original ?: Cerveza()).copy(
            nombre = form.nombre,
            cerveceria = form.cerveceria,
            estilo = form.estilo,
            abv = abv,
            ibu = ibu,
            puntuacion = form.puntuacion,
            lugar = form.lugar,
            notas = form.notas,
            ubicacion = form.ubicacion,
            foto = form.foto,
        )
        viewModelScope.launch {
            _uiState.update { it.copy(guardando = true, error = null) }
            val resultado = guardarCerveza(cerveza)
            _uiState.update {
                when (resultado) {
                    is AppResult.Success -> it.copy(guardando = false, guardada = true)
                    is AppResult.Error -> it.copy(guardando = false, error = resultado.error)
                }
            }
        }
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                CervezaFormViewModel(
                    cervezaId = createSavedStateHandle().toRoute<FormularioCerveza>().cervezaId,
                    obtenerCerveza = c.obtenerCerveza,
                    guardarCerveza = c.guardarCerveza,
                    obtenerUbicacionActual = c.obtenerUbicacionActual,
                    procesarFoto = c.procesarFoto,
                )
            }
        }
    }
}
