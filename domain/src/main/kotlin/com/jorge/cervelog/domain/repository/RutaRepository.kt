package com.jorge.cervelog.domain.repository

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.ModoTransporte
import com.jorge.cervelog.domain.model.Ruta

interface RutaRepository {

    suspend fun calcularRuta(origen: Coordenadas, destino: Coordenadas, modo: ModoTransporte): AppResult<Ruta>
}
