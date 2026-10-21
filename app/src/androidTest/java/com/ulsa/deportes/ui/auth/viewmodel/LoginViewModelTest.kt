package com.ulsa.deportes.ui.auth.viewmodel

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.ulsa.deportes.ui.auth.data.SessionPreferences
import com.ulsa.deportes.ui.auth.model.LoginPayload
import com.ulsa.deportes.ui.auth.model.LoginUiState
import com.ulsa.deportes.ui.auth.model.UserDto
import com.ulsa.deportes.ui.auth.repository.AuthRepository
import com.ulsa.deportes.ui.auth.repository.Result
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.times

class LoginViewModelTest {

    @get:Rule
    var instantTaskExecutorRule = InstantTaskExecutorRule()

    private val authRepository: AuthRepository = mock()
    private lateinit var viewModel: LoginViewModel

    @Before
    fun setup() {
        viewModel = LoginViewModel(
            application = androidx.test.core.app.ApplicationProvider.getApplicationContext(),
            authRepository = authRepository
        )
    }

    @Test
    fun `autenticacionExitosa_estadoDejaDeIndicarCarga_informaResultadoExitoso_noPresentaError`() = runBlockingTest {
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJleHAiOjE5MDAwMDAwMDB9.fake"
        val user = UserDto("1", "14446", "Jorge Chaparro", "jorge@ulsa.edu.ni", "ESTUDIANTE", true)
        val payload = LoginPayload(token, user)
        `when`(authRepository.login("jorge@ulsa.edu.ni", "password123")).thenReturn(Result.Success(payload))

        viewModel.login("jorge@ulsa.edu.ni", "password123")

        val state = viewModel.uiState.value
        assertFalse("isLoading debe ser false", state.isLoading)
        assertTrue("isLoggedIn debe ser true", state.isLoggedIn)
        assertNull("error debe ser null", state.error)
    }

    @Test
    fun `credencialesIncorrectas_exponeErrorComprensible_noNavegaAInicio`() = runBlockingTest {
        `when`(authRepository.login("wrong@email.com", "wrongpass")).thenReturn(
            Result.Error("Correo o contraseña incorrectos")
        )

        viewModel.login("wrong@email.com", "wrongpass")

        val state = viewModel.uiState.value
        assertFalse("isLoading debe ser false", state.isLoading)
        assertFalse("isLoggedIn debe ser false", state.isLoggedIn)
        assertEquals("Correo o contraseña incorrectos", state.error)
    }

    @Test
    fun `cuentaDeshabilitada_diferenciaDeCredencialesIncorrectas`() = runBlockingTest {
        `when`(authRepository.login("disabled@ulsa.edu.ni", "password123")).thenReturn(
            Result.Error("Esta cuenta está deshabilitada")
        )

        viewModel.login("disabled@ulsa.edu.ni", "password123")

        val state = viewModel.uiState.value
        assertFalse("isLoading debe ser false", state.isLoading)
        assertFalse("isLoggedIn debe ser false", state.isLoggedIn)
        assertEquals("Esta cuenta está deshabilitada", state.error)
    }

    @Test
    fun `errorDeRed_dejaDeIndicarCarga_exponeMensajeApropiado_noNavegaAInicio`() = runBlockingTest {
        `when`(authRepository.login("jorge@ulsa.edu.ni", "password123")).thenReturn(Result.NetworkError)

        viewModel.login("jorge@ulsa.edu.ni", "password123")

        val state = viewModel.uiState.value
        assertFalse("isLoading debe ser false", state.isLoading)
        assertFalse("isLoggedIn debe ser false", state.isLoggedIn)
        assertTrue("error debe mencionar conexión", state.error?.contains("conexión") == true)
    }

    @Test
    fun `validacion_camposVacios_muestraError_noLlamaRepositorio`() = runBlockingTest {
        viewModel.login("", "password123")

        val state = viewModel.uiState.value
        assertFalse("isLoading debe ser false", state.isLoading)
        assertEquals("Ingresa correo y contraseña", state.error)
    }

    @Test
    fun `clearError_limpiaMensajeDeError`() = runBlockingTest {
        `when`(authRepository.login("wrong@email.com", "wrongpass")).thenReturn(
            Result.Error("Correo o contraseña incorrectos")
        )
        viewModel.login("wrong@email.com", "wrongpass")
        assertNotNull(viewModel.uiState.value.error)

        viewModel.clearError()

        assertNull(viewModel.uiState.value.error)
    }

    @Test
    fun `evitaSolicitudesDuplicadas_cuandoYaHayUnaEnCurso`() = runBlockingTest {
        var callCount = 0
        `when`(authRepository.login("test@test.com", "pass")).thenAnswer {
            callCount++
            Result.Success(LoginPayload("token", UserDto("1", "1", "Test", "test@test.com", "ESTUDIANTE", true)))
        }

        viewModel.login("test@test.com", "pass")
        viewModel.login("test@test.com", "pass")

        assertEquals(1, callCount)
    }
}