package com.jorge.cervelog.domain.usecase

import com.jorge.cervelog.domain.model.AppResult
import com.jorge.cervelog.domain.model.DomainError
import com.jorge.cervelog.domain.testing.FakeAuthRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class SesionUseCasesTest {

    private val auth = FakeAuthRepository()

    @Test
    fun `iniciar sesion valida antes de llamar a Firebase`() = runTest {
        val iniciar = IniciarSesionUseCase(auth)

        assertEquals(AppResult.Error(DomainError.EmailNoValido), iniciar("no-es-un-email", "x"))
        assertEquals(AppResult.Error(DomainError.ContrasenaVacia), iniciar("ana@aplicaciones.com", ""))
        assertEquals(0, auth.llamadas)

        assertEquals(AppResult.Success(Unit), iniciar("  ana@aplicaciones.com ", "cervelog123"))
        assertEquals(1, auth.llamadas)
    }

    @Test
    fun `registrarse exige 6 caracteres y que coincidan`() = runTest {
        val registrarse = RegistrarseUseCase(auth)

        assertEquals(AppResult.Error(DomainError.ContrasenaCorta), registrarse("a@b.com", "123", "123"))
        assertEquals(AppResult.Error(DomainError.ContrasenasDistintas), registrarse("a@b.com", "123456", "654321"))
        assertEquals(AppResult.Success(Unit), registrarse("a@b.com", "123456", "123456"))
    }

    @Test
    fun `al registrarse se envia el correo de confirmacion`() = runTest {
        RegistrarseUseCase(auth)("a@b.com", "123456", "123456")

        assertEquals(1, auth.correosEnviados)
    }

    @Test
    fun `si el registro falla no se envia correo`() = runTest {
        auth.resultado = AppResult.Error(DomainError.EmailYaRegistrado)

        RegistrarseUseCase(auth)("a@b.com", "123456", "123456")

        assertEquals(0, auth.correosEnviados)
    }

    @Test
    fun `comprobar la verificacion devuelve el estado actualizado`() = runTest {
        val comprobar = ComprobarVerificacionUseCase(auth)
        assertEquals(false, (comprobar() as AppResult.Success).data.emailVerificado)

        auth.confirmarCorreo()

        assertEquals(true, (comprobar() as AppResult.Success).data.emailVerificado)
    }

    @Test
    fun `el error de Firebase llega tal cual`() = runTest {
        auth.resultado = AppResult.Error(DomainError.CredencialesIncorrectas)

        assertEquals(auth.resultado, IniciarSesionUseCase(auth)("ana@aplicaciones.com", "mal"))
    }
}
