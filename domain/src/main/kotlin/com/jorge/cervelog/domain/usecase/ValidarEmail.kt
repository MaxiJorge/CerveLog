package com.jorge.cervelog.domain.usecase

private val FORMATO_EMAIL = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

internal fun esEmailValido(email: String) = FORMATO_EMAIL.matches(email)
