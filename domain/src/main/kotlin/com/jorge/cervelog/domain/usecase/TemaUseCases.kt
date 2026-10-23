package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.Tema
import com.jorge.cervelog.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.StateFlow

class ObservarTemaUseCase(private val repository: PreferenciasRepository) {
    operator fun invoke(): StateFlow<Tema> = repository.tema
}

class CambiarTemaUseCase(private val repository: PreferenciasRepository) {
    operator fun invoke(tema: Tema) = repository.guardarTema(tema)
}
