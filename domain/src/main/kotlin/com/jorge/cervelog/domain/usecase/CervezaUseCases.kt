package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.repository.CervezaRepository
import kotlinx.coroutines.flow.Flow

// Sin lógica propia, para que los ViewModels solo dependan de casos de uso

class ObservarCervezasUseCase(private val repository: CervezaRepository) {
    operator fun invoke(): Flow<AppResult<List<Cerveza>>> = repository.observarCervezas()
}

class ObservarCervezaUseCase(private val repository: CervezaRepository) {
    operator fun invoke(id: String): Flow<AppResult<Cerveza>> = repository.observarCerveza(id)
}

class ObtenerCervezaUseCase(private val repository: CervezaRepository) {
    suspend operator fun invoke(id: String): AppResult<Cerveza> = repository.obtenerCerveza(id)
}

class EliminarCervezaUseCase(private val repository: CervezaRepository) {
    suspend operator fun invoke(id: String): AppResult<Unit> = repository.eliminarCerveza(id)
}
