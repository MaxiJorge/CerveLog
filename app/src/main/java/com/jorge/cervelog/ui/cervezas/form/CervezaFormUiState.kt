package com.jorge.cervelog.ui.cervezas.form

import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.DomainError

// ABV e IBU son texto hasta validarlos
data class CervezaForm(
    val nombre: String = "",
    val cerveceria: String = "",
    val estilo: String = "",
    val abv: String = "",
    val ibu: String = "",
    val puntuacion: Int = 0,
    val lugar: String = "",
    val notas: String = "",
    val ubicacion: Coordenadas? = null,
    val foto: String? = null,
)

data class CervezaFormUiState(
    val form: CervezaForm = CervezaForm(),
    val cargando: Boolean = false,
    val guardando: Boolean = false,
    val obteniendoUbicacion: Boolean = false,
    val procesandoFoto: Boolean = false,
    val error: DomainError? = null,
    val guardada: Boolean = false,
)

fun Cerveza.toForm() = CervezaForm(
    nombre = nombre,
    cerveceria = cerveceria,
    estilo = estilo,
    // Coma decimal; al guardar se acepta coma o punto
    abv = abv?.toString()?.removeSuffix(".0")?.replace('.', ',').orEmpty(),
    ibu = ibu?.toString().orEmpty(),
    puntuacion = puntuacion,
    lugar = lugar,
    notas = notas,
    ubicacion = ubicacion,
    foto = foto,
)
