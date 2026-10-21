package com.jorge.cervelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.jorge.cervelog.data.model.Cerveza
import com.jorge.cervelog.data.model.Publicacion
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class CervezaRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance(),
) {
    private fun cervezasDelUsuario(): CollectionReference {
        val uid = auth.currentUser?.uid ?: error("No hay un usuario autenticado")
        return db.collection("usuarios").document(uid).collection("cervezas")
    }

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

    suspend fun obtenerCerveza(id: String): Cerveza? =
        cervezasDelUsuario().document(id).get().await().toObject(Cerveza::class.java)

    // Si tiene ubicación, se publica también en el mapa comunitario con el mismo id.
    // El batch hace que se guarden las dos o ninguna.
    suspend fun guardarCerveza(cerveza: Cerveza) {
        val coleccion = cervezasDelUsuario()
        val documento = if (cerveza.id.isBlank()) coleccion.document() else coleccion.document(cerveza.id)
        val batch = db.batch().set(documento, cerveza)

        val usuario = auth.currentUser ?: error("No hay un usuario autenticado")
        if (cerveza.latitud != null && cerveza.longitud != null) {
            val publicacion = Publicacion(
                autorUid = usuario.uid,
                autorEmail = usuario.email.orEmpty(),
                nombre = cerveza.nombre,
                cerveceria = cerveza.cerveceria,
                estilo = cerveza.estilo,
                abv = cerveza.abv,
                ibu = cerveza.ibu,
                puntuacion = cerveza.puntuacion,
                lugar = cerveza.lugar,
                latitud = cerveza.latitud,
                longitud = cerveza.longitud,
                fecha = cerveza.fecha,
            )
            batch.set(db.collection("publicaciones").document(documento.id), publicacion)
        }
        batch.commit().await()
    }

    // Se borra también su publicación del mapa, si la tiene
    suspend fun eliminarCerveza(id: String) {
        db.batch()
            .delete(cervezasDelUsuario().document(id))
            .delete(db.collection("publicaciones").document(id))
            .commit()
            .await()
    }
}
