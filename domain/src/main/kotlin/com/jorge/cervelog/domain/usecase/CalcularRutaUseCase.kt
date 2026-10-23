package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.ModoTransporte
import com.jorge.cervelog.domain.model.Ruta
import com.jorge.cervelog.domain.repository.RutaRepository

// En línea recta; por calles suele salir algo más
const val MAX_METROS_A_PIE = 2_000.0

class CalcularRutaUseCase(private val repository: RutaRepository) {
    suspend operator fun invoke(origen: Coordenadas, destino: Coordenadas): AppResult<Ruta> {
        val modo = if (origen.distanciaMetrosHasta(destino) <= MAX_METROS_A_PIE) ModoTransporte.A_PIE
        else ModoTransporte.EN_COCHE
        return repository.calcularRuta(origen, destino, modo)
    }
}
