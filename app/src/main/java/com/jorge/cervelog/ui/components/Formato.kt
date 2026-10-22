package com.jorge.cervelog.ui.components

import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

val LOCALE_ES: Locale = Locale.forLanguageTag("es-ES")

private val formatoFecha = DateTimeFormatter.ofPattern("d MMM yyyy", LOCALE_ES)

fun Instant.formatearFecha(): String = atZone(ZoneId.systemDefault()).toLocalDate().format(formatoFecha)

fun Double.formatearAbv(): String = String.format(LOCALE_ES, "%.1f%%", this)

fun Double.formatearDecimal(): String = String.format(LOCALE_ES, "%.1f", this)

fun Double.formatearKm(): String = String.format(LOCALE_ES, "%.1f km", this / 1000)

fun subtitulo(vararg partes: String): String = partes.filter { it.isNotBlank() }.joinToString(" · ")
