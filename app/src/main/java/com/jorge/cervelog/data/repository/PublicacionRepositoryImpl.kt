package com.jorge.cervelog.data.repository

import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.snapshots
import com.jorge.cervelog.data.firestore.COLECCION_PUBLICACIONES
import com.jorge.cervelog.data.firestore.PublicacionDocumento
import com.jorge.cervelog.data.mapper.resultadoDe
import com.jorge.cervelog.data.mapper.toDomain
import com.jorge.cervelog.data.mapper.toTimestamp
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.model.Zona
import com.jorge.cervelog.domain.repository.PublicacionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.transform
import kotlinx.coroutines.tasks.await
import java.time.Instant

private const val MAX_PUBLICACIONES = 300L

class PublicacionRepositoryImpl(private val db: FirebaseFirestore) : PublicacionRepository {

    // Firestore filtra la latitud y aquí la longitud, así basta el índice automático
    override suspend fun buscarEnZona(zona: Zona): AppResult<List<Publicacion>> = resultadoDe {
        db.collection(COLECCION_PUBLICACIONES)
            .whereGreaterThanOrEqualTo("latitud", zona.sur)
            .whereLessThanOrEqualTo("latitud", zona.norte)
            .limit(MAX_PUBLICACIONES)
            .get()
            .await()
            .toObjects(PublicacionDocumento::class.java)
            .map { it.toDomain() }
            .filter { zona.contiene(it.ubicacion) }
    }

    override fun observarNuevas(desde: Instant): Flow<Publicacion> =
        db.collection(COLECCION_PUBLICACIONES)
            .whereGreaterThan("fecha", desde.toTimestamp())
            .snapshots()
            .transform { snapshot ->
                snapshot.documentChanges
                    .filter { it.type == DocumentChange.Type.ADDED }
                    .forEach { emit(it.document.toObject(PublicacionDocumento::class.java).toDomain()) }
            }
            // Si falla la escucha, simplemente dejan de llegar
            .catch { }
}
