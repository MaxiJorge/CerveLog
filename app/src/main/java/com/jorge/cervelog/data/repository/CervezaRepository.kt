package com.jorge.cervelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jorge.cervelog.data.model.Cerveza
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/**
 * Acceso a las cervezas del usuario autenticado.
 * Cada usuario tiene su propia subcolección (usuarios/{uid}/cervezas), así que
 * es imposible consultar datos de otro usuario desde aquí, y las reglas de
 * Firestore lo refuerzan del lado del servidor.
 */
class CervezaRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun cervezasDelUsuario(): CollectionReference {
        val uid = auth.currentUser?.uid ?: error("No hay un usuario autenticado")
        return db.collection("usuarios").document(uid).collection("cervezas")
    }

    /** Lista en tiempo real, de la más reciente a la más antigua. */
    fun observarCervezas(): Flow<List<Cerveza>> = callbackFlow {
        val registration = cervezasDelUsuario()
            .orderBy("fecha", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObjects(Cerveza::class.java).orEmpty())
            }
        awaitClose { registration.remove() }
    }

    /** Un único documento en tiempo real; emite null si no existe (p. ej. fue borrado). */
    fun observarCerveza(id: String): Flow<Cerveza?> = callbackFlow {
        val registration = cervezasDelUsuario().document(id)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Cerveza::class.java))
            }
        awaitClose { registration.remove() }
    }

    /** Lectura puntual, para precargar el formulario de edición. Devuelve null si no existe. */
    suspend fun obtenerCerveza(id: String): Cerveza? =
        cervezasDelUsuario().document(id).get().await().toObject(Cerveza::class.java)

    /**
     * Crea la cerveza si no tiene id, o reemplaza el documento existente si lo tiene.
     * El campo id no se escribe en el documento (@DocumentId): es el nombre del documento.
     */
    suspend fun guardarCerveza(cerveza: Cerveza) {
        val coleccion = cervezasDelUsuario()
        val documento = if (cerveza.id.isBlank()) coleccion.document() else coleccion.document(cerveza.id)
        documento.set(cerveza).await()
    }
}
