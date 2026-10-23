package com.jorge.cervelog.data.repository

import android.content.Context
import androidx.core.content.edit
import com.jorge.cervelog.domain.model.Tema
import com.jorge.cervelog.domain.repository.PreferenciasRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val ARCHIVO = "preferencias"
private const val CLAVE_TEMA = "tema"

// SharedPreferences basta para un solo ajuste y viene con Android
class PreferenciasRepositoryImpl(context: Context) : PreferenciasRepository {

    private val preferencias = context.getSharedPreferences(ARCHIVO, Context.MODE_PRIVATE)

    private val _tema = MutableStateFlow(leerTema())
    override val tema: StateFlow<Tema> = _tema.asStateFlow()

    override fun guardarTema(tema: Tema) {
        preferencias.edit { putString(CLAVE_TEMA, tema.name) }
        _tema.value = tema
    }

    private fun leerTema(): Tema =
        Tema.entries.find { it.name == preferencias.getString(CLAVE_TEMA, null) } ?: Tema.SISTEMA
}
