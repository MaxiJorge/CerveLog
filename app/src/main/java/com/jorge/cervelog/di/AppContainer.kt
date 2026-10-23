package com.jorge.cervelog.di

import android.content.Context
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.jorge.cervelog.BuildConfig
import com.jorge.cervelog.CerveLogApp
import com.jorge.cervelog.data.repository.AuthRepositoryImpl
import com.jorge.cervelog.data.repository.CervezaRepositoryImpl
import com.jorge.cervelog.data.repository.FotoRepositoryImpl
import com.jorge.cervelog.data.repository.PreferenciasRepositoryImpl
import com.jorge.cervelog.data.repository.PublicacionRepositoryImpl
import com.jorge.cervelog.data.repository.RutaRepositoryImpl
import com.jorge.cervelog.data.repository.UbicacionRepositoryImpl
import com.jorge.cervelog.domain.repository.AuthRepository
import com.jorge.cervelog.domain.repository.CervezaRepository
import com.jorge.cervelog.domain.repository.FotoRepository
import com.jorge.cervelog.domain.repository.PreferenciasRepository
import com.jorge.cervelog.domain.repository.PublicacionRepository
import com.jorge.cervelog.domain.repository.RutaRepository
import com.jorge.cervelog.domain.repository.UbicacionRepository
import com.jorge.cervelog.domain.usecase.BuscarPublicacionesEnZonaUseCase
import com.jorge.cervelog.domain.usecase.CalcularRutaUseCase
import com.jorge.cervelog.domain.usecase.CambiarTemaUseCase
import com.jorge.cervelog.domain.usecase.CerrarSesionUseCase
import com.jorge.cervelog.domain.usecase.ComprobarVerificacionUseCase
import com.jorge.cervelog.domain.usecase.EliminarCervezaUseCase
import com.jorge.cervelog.domain.usecase.GuardarCervezaUseCase
import com.jorge.cervelog.domain.usecase.IniciarSesionConGoogleUseCase
import com.jorge.cervelog.domain.usecase.IniciarSesionUseCase
import com.jorge.cervelog.domain.usecase.ObservarCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObservarCervezasCercanasUseCase
import com.jorge.cervelog.domain.usecase.ObservarCervezasUseCase
import com.jorge.cervelog.domain.usecase.ObservarEstadisticasUseCase
import com.jorge.cervelog.domain.usecase.ObservarSesionUseCase
import com.jorge.cervelog.domain.usecase.ObservarTemaUseCase
import com.jorge.cervelog.domain.usecase.ObtenerCervezaUseCase
import com.jorge.cervelog.domain.usecase.ObtenerPublicacionUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUbicacionActualUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUsuarioActualUseCase
import com.jorge.cervelog.domain.usecase.ProcesarFotoUseCase
import com.jorge.cervelog.domain.usecase.ReenviarCorreoVerificacionUseCase
import com.jorge.cervelog.domain.usecase.RegistrarseUseCase

// Único sitio que conoce las implementaciones. Cada repositorio se crea una sola vez.
class AppContainer(context: Context) {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    private val authRepository: AuthRepository = AuthRepositoryImpl(auth)
    private val cervezaRepository: CervezaRepository = CervezaRepositoryImpl(auth, db)
    private val publicacionRepository: PublicacionRepository = PublicacionRepositoryImpl(db)
    private val ubicacionRepository: UbicacionRepository = UbicacionRepositoryImpl(context)
    private val fotoRepository: FotoRepository = FotoRepositoryImpl(context)
    private val preferenciasRepository: PreferenciasRepository = PreferenciasRepositoryImpl(context)
    private val rutaRepository: RutaRepository = RutaRepositoryImpl(context, BuildConfig.MAPS_API_KEY)

    val iniciarSesion get() = IniciarSesionUseCase(authRepository)
    val iniciarSesionConGoogle get() = IniciarSesionConGoogleUseCase(authRepository)
    val registrarse get() = RegistrarseUseCase(authRepository)
    val reenviarCorreoVerificacion get() = ReenviarCorreoVerificacionUseCase(authRepository)
    val comprobarVerificacion get() = ComprobarVerificacionUseCase(authRepository)
    val cerrarSesion get() = CerrarSesionUseCase(authRepository)
    val observarSesion get() = ObservarSesionUseCase(authRepository)
    val obtenerUsuarioActual get() = ObtenerUsuarioActualUseCase(authRepository)

    val observarCervezas get() = ObservarCervezasUseCase(cervezaRepository)
    val observarCerveza get() = ObservarCervezaUseCase(cervezaRepository)
    val obtenerCerveza get() = ObtenerCervezaUseCase(cervezaRepository)
    val guardarCerveza get() = GuardarCervezaUseCase(cervezaRepository, authRepository)
    val eliminarCerveza get() = EliminarCervezaUseCase(cervezaRepository)
    val observarEstadisticas get() = ObservarEstadisticasUseCase(cervezaRepository)

    val buscarPublicacionesEnZona get() = BuscarPublicacionesEnZonaUseCase(publicacionRepository)
    val obtenerPublicacion get() = ObtenerPublicacionUseCase(publicacionRepository)
    val calcularRuta get() = CalcularRutaUseCase(rutaRepository)
    val obtenerUbicacionActual get() = ObtenerUbicacionActualUseCase(ubicacionRepository)
    val procesarFoto get() = ProcesarFotoUseCase(fotoRepository)
    val observarCervezasCercanas get() =
        ObservarCervezasCercanasUseCase(publicacionRepository, ubicacionRepository, authRepository)

    val observarTema get() = ObservarTemaUseCase(preferenciasRepository)
    val cambiarTema get() = CambiarTemaUseCase(preferenciasRepository)
}

fun CreationExtras.contenedor(): AppContainer = (checkNotNull(this[APPLICATION_KEY]) as CerveLogApp).container
