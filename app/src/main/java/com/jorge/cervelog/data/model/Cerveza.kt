package com.jorge.cervelog.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

/**
 * Una cerveza registrada por el usuario.
 * Se guarda en Firestore en usuarios/{uid}/cervezas/{id}.
 * Los valores por defecto son necesarios para que Firestore pueda deserializar con toObject().
 */
data class Cerveza(
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
    val fotoUrl: String? = null,
    val fecha: Timestamp? = null,
)
