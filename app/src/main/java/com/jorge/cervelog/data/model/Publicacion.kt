package com.jorge.cervelog.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

// Copia pública y ligera de una cerveza para el mapa (sin foto ni notas)
data class Publicacion(
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
