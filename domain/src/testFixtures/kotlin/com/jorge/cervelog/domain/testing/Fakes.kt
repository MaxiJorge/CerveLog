package com.jorge.cervelog.domain.testing

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.Cerveza
import com.jorge.cervelog.domain.model.Coordenadas
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.ModoTransporte
import com.jorge.cervelog.domain.model.Publicacion
import com.jorge.cervelog.domain.model.Ruta
import com.jorge.cervelog.domain.model.Usuario
import com.jorge.cervelog.domain.model.Zona
import com.jorge.cervelog.domain.repository.AuthRepository
import com.jorge.cervelog.domain.repository.CervezaRepository
import com.jorge.cervelog.domain.repository.FotoRepository
import com.jorge.cervelog.domain.repository.PublicacionRepository
import com.jorge.cervelog.domain.repository.RutaRepository
import com.jorge.cervelog.domain.repository.UbicacionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import java.time.Duration
import java.time.Instant

// Repositorios en memoria que se comportan como los reales

class FakeAuthRepository(usuario: Usuario? = usuario()) : AuthRepository {
    private val sesion = MutableStateFlow(usuario)
    var resultado: AppResult<Unit> = AppResult.Success(Unit)
    var llamadas = 0
        private set

    override val usuarioActual: Usuario? get() = sesion.value

    override fun observarSesion(): Flow<Usuario?> = sesion

    override suspend fun iniciarSesion(email: String, contrasena: String): AppResult<Unit> {
        llamadas++
        return resultado
    }

    override suspend fun iniciarSesionConGoogle(idToken: String): AppResult<Unit> {
        llamadas++
        return resultado
    }

    override suspend fun registrarse(email: String, contrasena: String): AppResult<Unit> {
        llamadas++
        return resultado
    }

    var correosEnviados = 0
        private set

    override suspend fun enviarCorreoVerificacion(): AppResult<Unit> {
        correosEnviados++
        return AppResult.Success(Unit)
    }

    // Simula que el usuario ha pulsado el enlace del correo
    fun confirmarCorreo() = sesion.update { it?.copy(emailVerificado = true) }

    override suspend fun recargarUsuario(): AppResult<Usuario> =
        sesion.value?.let { AppResult.Success(it) } ?: AppResult.Error(DomainError.SinSesion)

    override fun cerrarSesion() {
        sesion.value = null
    }
}

class FakeCervezaRepository(iniciales: List<Cerveza> = emptyList()) : CervezaRepository {
    val cervezas = MutableStateFlow(iniciales)
    val publicaciones = mutableMapOf<String, Publicacion>()

    // Simula un fallo de Firestore
    var error: DomainError? = null

    override fun observarCervezas(): Flow<AppResult<List<Cerveza>>> {
        error?.let { return flowOf(AppResult.Error(it)) }
        return cervezas.map { lista -> AppResult.Success(lista.sortedByDescending { it.fecha }) }
    }

    override fun observarCerveza(id: String): Flow<AppResult<Cerveza>> = cervezas.map { lista ->
        lista.find { it.id == id }?.let { AppResult.Success(it) } ?: AppResult.Error(DomainError.NoEncontrada)
    }

    override suspend fun obtenerCerveza(id: String): AppResult<Cerveza> {
        error?.let { return AppResult.Error(it) }
        return cervezas.value.find { it.id == id }?.let { AppResult.Success(it) }
            ?: AppResult.Error(DomainError.NoEncontrada)
    }

    override suspend fun guardarCerveza(cerveza: Cerveza, publicacion: Publicacion?): AppResult<Unit> {
        error?.let { return AppResult.Error(it) }
        val id = cerveza.id.ifBlank { "id${cervezas.value.size + 1}" }
        cervezas.update { lista -> lista.filterNot { it.id == id } + cerveza.copy(id = id) }
        if (publicacion != null) publicaciones[id] = publicacion.copy(id = id)
        return AppResult.Success(Unit)
    }

    override suspend fun eliminarCerveza(id: String): AppResult<Unit> {
        error?.let { return AppResult.Error(it) }
        cervezas.update { lista -> lista.filterNot { it.id == id } }
        publicaciones.remove(id)
        return AppResult.Success(Unit)
    }
}

class FakePublicacionRepository(var guardadas: List<Publicacion> = emptyList()) : PublicacionRepository {
    val nuevas = MutableSharedFlow<Publicacion>(replay = 10)

    override suspend fun buscarEnZona(zona: Zona): AppResult<List<Publicacion>> =
        AppResult.Success(guardadas.filter { zona.contiene(it.ubicacion) })

    override suspend fun obtenerPublicacion(id: String): AppResult<Publicacion> =
        guardadas.find { it.id == id }?.let { AppResult.Success(it) } ?: AppResult.Error(DomainError.NoEncontrada)

    override fun observarNuevas(desde: Instant): Flow<Publicacion> = nuevas
}

class FakeUbicacionRepository(var resultado: AppResult<Coordenadas> = AppResult.Success(PALERMO)) : UbicacionRepository {
    override suspend fun obtenerUbicacionActual(): AppResult<Coordenadas> = resultado
    override suspend fun obtenerUbicacionAproximada(): AppResult<Coordenadas> = resultado
}

class FakeFotoRepository(var resultado: AppResult<String> = AppResult.Success("Zm90bw==")) : FotoRepository {
    override suspend fun procesarFoto(uri: String): AppResult<String> = resultado
}

class FakeRutaRepository(var error: DomainError? = null) : RutaRepository {
    var modoPedido: ModoTransporte? = null
        private set

    // Ruta en línea recta, suficiente para los tests
    override suspend fun calcularRuta(origen: Coordenadas, destino: Coordenadas, modo: ModoTransporte): AppResult<Ruta> {
        modoPedido = modo
        error?.let { return AppResult.Error(it) }
        val metros = origen.distanciaMetrosHasta(destino).toInt()
        return AppResult.Success(Ruta(listOf(origen, destino), metros, Duration.ofMinutes(metros / 80L), modo))
    }
}
