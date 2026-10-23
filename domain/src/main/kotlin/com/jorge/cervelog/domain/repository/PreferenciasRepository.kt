package com.jorge.cervelog.domain.repository

import com.jorge.cervelog.domain.model.Tema
import kotlinx.coroutines.flow.StateFlow

// Ajustes del dispositivo, no de la cuenta: no se guardan en Firestore
interface PreferenciasRepository {

    // StateFlow para tener el valor desde el primer fotograma y no ver el tema equivocado al abrir
    val tema: StateFlow<Tema>

    fun guardarTema(tema: Tema)
}
