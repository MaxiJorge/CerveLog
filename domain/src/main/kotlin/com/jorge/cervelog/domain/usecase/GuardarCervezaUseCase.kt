package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.toPublicacion
import com.jorge.cervelog.domain.repository.AuthRepository
import com.jorge.cervelog.domain.repository.CervezaRepository
import java.time.Instant

class GuardarCervezaUseCase(
    private val cervezaRepository: CervezaRepository,
    private val authRepository: AuthRepository,
    private val ahora: () -> Instant = Instant::now,
) {
    suspend operator fun invoke(cerveza: Cerveza): AppResult<Unit> {
        val autor = authRepository.usuarioActual ?: return AppResult.Error(DomainError.SinSesion)
        val limpia = cerveza.copy(
            nombre = cerveza.nombre.trim(),
            cerveceria = cerveza.cerveceria.trim(),
            estilo = cerveza.estilo.trim(),
            lugar = cerveza.lugar.trim(),
            notas = cerveza.notas.trim(),
            // Al editar se conserva la fecha original
            fecha = cerveza.fecha ?: ahora(),
        )
        validar(limpia)?.let { return AppResult.Error(it) }

        val publicacion = limpia.ubicacion?.let { limpia.toPublicacion(autor, it) }
        return cervezaRepository.guardarCerveza(limpia, publicacion)
    }

    private fun validar(cerveza: Cerveza): DomainError? = when {
        cerveza.nombre.isBlank() -> DomainError.NombreVacio
        cerveza.puntuacion !in 1..5 -> DomainError.PuntuacionNoValida
        cerveza.abv != null && cerveza.abv !in 0.0..100.0 -> DomainError.AbvNoValido
        cerveza.ibu != null && cerveza.ibu !in 0..1000 -> DomainError.IbuNoValido
        else -> null
    }
}
