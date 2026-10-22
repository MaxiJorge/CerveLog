package com.jorge.cervelog.domain.model

data class Usuario(val uid: String, val email: String, val emailVerificado: Boolean = false)
