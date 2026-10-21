package com.jorge.cervelog.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Los valores por defecto los necesita Firestore para toObject()
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
    // JPEG reducido codificado en Base64
    val foto: String? = null,
    val fecha: Timestamp? = null,
)
