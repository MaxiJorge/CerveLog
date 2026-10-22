package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.Estadisticas
import com.jorge.cervelog.domain.model.map
import com.jorge.cervelog.domain.repository.CervezaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Se calculan sobre la consulta del listado, sin guardar contadores
class ObservarEstadisticasUseCase(private val repository: CervezaRepository) {
    operator fun invoke(): Flow<AppResult<Estadisticas>> =
        repository.observarCervezas().map { resultado -> resultado.map(::calcular) }

    private fun calcular(cervezas: List<Cerveza>) = Estadisticas(
        total = cervezas.size,
        enElMapa = cervezas.count { it.ubicacion != null },
        puntuacionMedia = cervezas.map { it.puntuacion }.average().takeIf { cervezas.isNotEmpty() },
    )
}
