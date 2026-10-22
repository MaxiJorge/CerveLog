package com.jorge.cervelog.domain.repository

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas

// El permiso se pide en la pantalla antes de llamar aquí
interface UbicacionRepository {

    suspend fun obtenerUbicacionActual(): AppResult<Coordenadas>

    // La última conocida si existe; si no, una nueva
    suspend fun obtenerUbicacionAproximada(): AppResult<Coordenadas>
}
