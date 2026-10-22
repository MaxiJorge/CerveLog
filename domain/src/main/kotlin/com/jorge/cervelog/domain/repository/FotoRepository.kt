package com.jorge.cervelog.domain.repository

import com.jorge.cervelog.domain.model.AppResult

interface FotoRepository {

    // Devuelve la foto reducida en Base64
    suspend fun procesarFoto(uri: String): AppResult<String>
}
