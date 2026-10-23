package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.model.Zona
import com.jorge.cervelog.domain.repository.FotoRepository
import com.jorge.cervelog.domain.repository.PublicacionRepository
import com.jorge.cervelog.domain.repository.UbicacionRepository

class BuscarPublicacionesEnZonaUseCase(private val repository: PublicacionRepository) {
    suspend operator fun invoke(zona: Zona): AppResult<List<Publicacion>> = repository.buscarEnZona(zona)
}

class ObtenerPublicacionUseCase(private val repository: PublicacionRepository) {
    suspend operator fun invoke(id: String): AppResult<Publicacion> = repository.obtenerPublicacion(id)
}

class ObtenerUbicacionActualUseCase(private val repository: UbicacionRepository) {
    suspend operator fun invoke(): AppResult<Coordenadas> = repository.obtenerUbicacionActual()
}

class ProcesarFotoUseCase(private val repository: FotoRepository) {
    suspend operator fun invoke(uri: String): AppResult<String> = repository.procesarFoto(uri)
}
