package com.jorge.cervelog.data.firestore

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

const val COLECCION_USUARIOS = "usuarios"
const val COLECCION_CERVEZAS = "cervezas"
const val COLECCION_PUBLICACIONES = "publicaciones"

// Los valores por defecto los necesita toObject() de Firestore
data class CervezaDocumento(
    @DocumentId val id: String = "",
    val nombre: String = "",
    val cerveceria: String = "",
    val estilo: String = "",
    val abv: Double? = null,
    val ibu: Int? = null,
    val puntuacion: Int = 0,
    val notas: String = "",
    val lugar: String = "",
    val latitud: Double? = null,
    val longitud: Double? = null,
    val foto: String? = null,
    val fecha: Timestamp? = null,
)

data class PublicacionDocumento(
    @DocumentId val id: String = "",
    val autorUid: String = "",
    val autorEmail: String = "",
    val nombre: String = "",
    val cerveceria: String = "",
    val estilo: String = "",
    val abv: Double? = null,
    val ibu: Int? = null,
    val puntuacion: Int = 0,
    val lugar: String = "",
    val latitud: Double = 0.0,
    val longitud: Double = 0.0,
    val fecha: Timestamp? = null,
)
