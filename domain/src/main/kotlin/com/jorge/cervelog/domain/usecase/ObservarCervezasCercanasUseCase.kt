package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.CervezaCercana
import com.jorge.cervelog.domain.repository.AuthRepository
import com.jorge.cervelog.domain.repository.PublicacionRepository
import com.jorge.cervelog.domain.repository.UbicacionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.mapNotNull
import java.time.Instant

const val RADIO_AVISO_METROS = 5_000.0

class ObservarCervezasCercanasUseCase(
    private val publicacionRepository: PublicacionRepository,
    private val ubicacionRepository: UbicacionRepository,
    private val authRepository: AuthRepository,
) {
    operator fun invoke(desde: Instant): Flow<CervezaCercana> {
        val miUid = authRepository.usuarioActual?.uid
        return publicacionRepository.observarNuevas(desde)
            .filter { it.autorUid != miUid }
            .mapNotNull { publicacion ->
                val yo = ubicacionRepository.obtenerUbicacionAproximada() as? AppResult.Success
                    ?: return@mapNotNull null
                val distancia = yo.data.distanciaMetrosHasta(publicacion.ubicacion)
                if (distancia <= RADIO_AVISO_METROS) CervezaCercana(publicacion, distancia) else null
            }
    }
}
