package com.jorge.cervelog.domain.repository

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.model.Zona
import kotlinx.coroutines.flow.Flow
import java.time.Instant

interface PublicacionRepository {

    suspend fun buscarEnZona(zona: Zona): AppResult<List<Publicacion>>

    // En tiempo real; si la escucha falla, el flujo termina
    fun observarNuevas(desde: Instant): Flow<Publicacion>
}
