package com.jorge.cervelog.ui.theme

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.jorge.cervelog.di.contenedor
import com.jorge.cervelog.domain.model.Tema
import com.jorge.cervelog.domain.usecase.CambiarTemaUseCase
import com.jorge.cervelog.domain.usecase.ObservarTemaUseCase
import kotlinx.coroutines.flow.StateFlow

// Lo usan la actividad, para pintar con el tema, y el perfil, para cambiarlo
class TemaViewModel(
    observarTema: ObservarTemaUseCase,
    private val cambiarTema: CambiarTemaUseCase,
) : ViewModel() {

    val tema: StateFlow<Tema> = observarTema()

    fun onTemaElegido(tema: Tema) = cambiarTema(tema)

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val c = contenedor()
                TemaViewModel(c.observarTema, c.cambiarTema)
            }
        }
    }
}
