package com.jorge.cervelog.domain.repository

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.Publicacion
import kotlinx.coroutines.flow.Flow

// Siempre las del usuario con sesión iniciada
interface CervezaRepository {

    // Ordenadas de la más reciente a la más antigua
    fun observarCervezas(): Flow<AppResult<List<Cerveza>>>

    fun observarCerveza(id: String): Flow<AppResult<Cerveza>>

    suspend fun obtenerCerveza(id: String): AppResult<Cerveza>

    // Guarda la cerveza y su publicación a la vez, o ninguna
    suspend fun guardarCerveza(cerveza: Cerveza, publicacion: Publicacion?): AppResult<Unit>

    // Borra también su publicación
    suspend fun eliminarCerveza(id: String): AppResult<Unit>
}
