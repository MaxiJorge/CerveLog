package com.jorge.cervelog.ui

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.model.Zona
import com.jorge.cervelog.domain.testing.FakeAuthRepository
import com.jorge.cervelog.domain.testing.FakePublicacionRepository
import com.jorge.cervelog.domain.testing.FakeUbicacionRepository
import com.jorge.cervelog.domain.testing.MainDispatcherRule
import com.jorge.cervelog.domain.testing.RECOLETA
import com.jorge.cervelog.domain.testing.publicacion
import com.jorge.cervelog.domain.usecase.BuscarPublicacionesEnZonaUseCase
import com.jorge.cervelog.domain.usecase.CerrarSesionUseCase
import com.jorge.cervelog.domain.usecase.ComprobarVerificacionUseCase
import com.jorge.cervelog.domain.usecase.ObservarEstadisticasUseCase
import com.jorge.cervelog.domain.usecase.ReenviarCorreoVerificacionUseCase
import com.jorge.cervelog.domain.testing.FakeCervezaRepository
import com.jorge.cervelog.domain.usecase.IniciarSesionConGoogleUseCase
import com.jorge.cervelog.domain.usecase.IniciarSesionUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUbicacionActualUseCase
import com.jorge.cervelog.domain.usecase.ObtenerUsuarioActualUseCase
import com.jorge.cervelog.domain.usecase.RegistrarseUseCase
import com.jorge.cervelog.ui.auth.AuthViewModel
import com.jorge.cervelog.ui.mapa.MapaComunitarioViewModel
import com.jorge.cervelog.ui.perfil.PerfilViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class OtrosViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `login traduce el error y deja de cargar`() {
        val auth = FakeAuthRepository().apply { resultado = AppResult.Error(DomainError.CredencialesIncorrectas) }
        val vm = AuthViewModel(IniciarSesionUseCase(auth), RegistrarseUseCase(auth), IniciarSesionConGoogleUseCase(auth))

        vm.onIniciarSesion("ana@aplicaciones.com", "mal")

        assertFalse(vm.uiState.value.cargando)
        assertEquals(DomainError.CredencialesIncorrectas, vm.uiState.value.error)
        vm.onErrorMostrado()
        assertNull(vm.uiState.value.error)
    }

    @Test
    fun `el mapa acumula lo descargado y quita lo borrado de la zona`() {
        val publicaciones = FakePublicacionRepository(listOf(publicacion(id = "a"), publicacion(id = "b")))
        val vm = MapaComunitarioViewModel(
            BuscarPublicacionesEnZonaUseCase(publicaciones),
            ObtenerUbicacionActualUseCase(FakeUbicacionRepository()),
            ObtenerUsuarioActualUseCase(FakeAuthRepository()),
        )
        val zona = Zona(sur = RECOLETA.latitud - 0.1, oeste = RECOLETA.longitud - 0.1, norte = RECOLETA.latitud + 0.1, este = RECOLETA.longitud + 0.1)

        vm.onZonaVisible(zona)
        assertEquals(setOf("a", "b"), vm.uiState.value.publicaciones.map { it.id }.toSet())

        publicaciones.guardadas = listOf(publicacion(id = "a"))
        vm.onZonaVisible(zona)
        assertEquals(listOf("a"), vm.uiState.value.publicaciones.map { it.id })
    }

    @Test
    fun `el perfil permite reenviar el correo de confirmacion`() = runTest {
        val auth = FakeAuthRepository()
        val vm = PerfilViewModel(
            ObservarEstadisticasUseCase(FakeCervezaRepository()),
            ObtenerUsuarioActualUseCase(auth),
            ComprobarVerificacionUseCase(auth),
            ReenviarCorreoVerificacionUseCase(auth),
            CerrarSesionUseCase(auth),
        )
        backgroundScope.launch(UnconfinedTestDispatcher(testScheduler)) { vm.uiState.collect() }

        vm.onReenviarCorreo()
        assertTrue(vm.uiState.value.correoEnviado)
        assertEquals(1, auth.correosEnviados)
        assertFalse(vm.uiState.value.emailVerificado)
    }
}
