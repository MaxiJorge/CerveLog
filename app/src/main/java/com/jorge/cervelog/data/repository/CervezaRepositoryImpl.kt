package com.jorge.cervelog.data.repository

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.snapshots
import com.jorge.cervelog.data.firestore.COLECCION_CERVEZAS
import com.jorge.cervelog.data.firestore.COLECCION_PUBLICACIONES
import com.jorge.cervelog.data.firestore.COLECCION_USUARIOS
import com.jorge.cervelog.data.firestore.CervezaDocumento
import com.jorge.cervelog.data.mapper.SinSesionException
import com.jorge.cervelog.data.mapper.resultadoDe
import com.jorge.cervelog.data.mapper.toDocumento
import com.jorge.cervelog.data.mapper.toDomain
import com.jorge.cervelog.data.mapper.toDomainError
import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.repository.CervezaRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await

class CervezaRepositoryImpl(
    private val auth: FirebaseAuth,
    private val db: FirebaseFirestore,
) : CervezaRepository {

    private fun cervezasDelUsuario(): CollectionReference {
        val uid = auth.currentUser?.uid ?: throw SinSesionException()
        return db.collection(COLECCION_USUARIOS).document(uid).collection(COLECCION_CERVEZAS)
    }

    override fun observarCervezas(): Flow<AppResult<List<Cerveza>>> =
        // Dentro de flow para que la falta de sesión llegue como error
        flow { emitAll(cervezasDelUsuario().orderBy("fecha", Query.Direction.DESCENDING).snapshots()) }
            .map<_, AppResult<List<Cerveza>>> { snapshot ->
                AppResult.Success(snapshot.toObjects(CervezaDocumento::class.java).map { it.toDomain() })
            }
            .catch { emit(AppResult.Error(it.toDomainError())) }

    override fun observarCerveza(id: String): Flow<AppResult<Cerveza>> =
        flow { emitAll(cervezasDelUsuario().document(id).snapshots()) }
            .map { snapshot ->
                snapshot.toObject(CervezaDocumento::class.java)?.let { AppResult.Success(it.toDomain()) }
                    ?: AppResult.Error(DomainError.NoEncontrada)
            }
            .catch { emit(AppResult.Error(it.toDomainError())) }

    override suspend fun obtenerCerveza(id: String): AppResult<Cerveza> {
        val resultado = resultadoDe {
            cervezasDelUsuario().document(id).get().await().toObject(CervezaDocumento::class.java)
        }
        return when (resultado) {
            is AppResult.Success -> resultado.data?.let { AppResult.Success(it.toDomain()) }
                ?: AppResult.Error(DomainError.NoEncontrada)
            is AppResult.Error -> resultado
        }
    }

    // Mismo id para la cerveza y su publicación; el batch guarda las dos o ninguna
    override suspend fun guardarCerveza(cerveza: Cerveza, publicacion: Publicacion?): AppResult<Unit> = resultadoDe<Unit> {
        val coleccion = cervezasDelUsuario()
        val documento = if (cerveza.id.isBlank()) coleccion.document() else coleccion.document(cerveza.id)
        val batch = db.batch().set(documento, cerveza.toDocumento())
        if (publicacion != null) {
            batch.set(db.collection(COLECCION_PUBLICACIONES).document(documento.id), publicacion.toDocumento())
        }
        batch.commit().await()
    }

    override suspend fun eliminarCerveza(id: String): AppResult<Unit> = resultadoDe<Unit> {
        db.batch()
            .delete(cervezasDelUsuario().document(id))
            .delete(db.collection(COLECCION_PUBLICACIONES).document(id))
            .commit()
            .await()
    }
}
