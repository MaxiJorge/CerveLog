package com.jorge.cervelog.data.repository

import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.jorge.cervelog.data.model.Publicacion
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

private const val MAX_PUBLICACIONES = 300L

class PublicacionRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    // Solo se descarga la zona visible del mapa. Firestore filtra por latitud
    // y la longitud se filtra aquí, así basta con el índice automático.
    suspend fun buscarEnZona(zona: LatLngBounds): List<Publicacion> =
        db.collection("publicaciones")
            .whereGreaterThanOrEqualTo("latitud", zona.southwest.latitude)
            .whereLessThanOrEqualTo("latitud", zona.northeast.latitude)
            .limit(MAX_PUBLICACIONES)
            .get()
            .await()
            .toObjects(Publicacion::class.java)
            .filter { zona.contains(LatLng(it.latitud, it.longitud)) }

    // Solo las que se publiquen a partir de `desde`, en tiempo real
    fun observarNuevas(desde: Timestamp): Flow<Publicacion> = callbackFlow {
        val registration = db.collection("publicaciones")
            .whereGreaterThan("fecha", desde)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                snapshot?.documentChanges
                    ?.filter { it.type == DocumentChange.Type.ADDED }
                    ?.forEach { trySend(it.document.toObject(Publicacion::class.java)) }
            }
        awaitClose { registration.remove() }
    }
}
