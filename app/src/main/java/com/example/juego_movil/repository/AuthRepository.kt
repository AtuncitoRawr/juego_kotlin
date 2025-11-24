package com.example.juego_movil.repository

import android.util.Log
import com.example.juego_movil.data.AuthRequest
import com.example.juego_movil.data.AuthState
import com.example.juego_movil.data.auth.AuthApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.lang.Exception

class AuthRepository(
    private val api: AuthApi,
    private val localProgressRepository: LocalProgressRepository
) {

    val authStateFlow: Flow<AuthState> = localProgressRepository.getAuthTokenFlow()
        .map { token ->
            if (!token.isNullOrBlank()) {
                // Asumimos sesión iniciada si hay token
                AuthState.SignedIn("User")
            } else {
                AuthState.SignedOut
            }
        }
        .onStart { emit(AuthState.Loading) }

    fun getCurrentAuthToken(): String? {
        return null // Placeholder, se usa el flow en AppRoot
    }

    // --- Métodos de API ---

    suspend fun login(request: AuthRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.login(request)
            val authResponse = response.body() ?: throw IllegalStateException("Respuesta vacía.")
            localProgressRepository.saveAuthToken(authResponse.authToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Login Error: ${e.message}")
            Result.failure(e)
        }
    }

    suspend fun signup(request: AuthRequest): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = api.signup(request)
            val authResponse = response.body() ?: throw IllegalStateException("Respuesta vacía.")
            localProgressRepository.saveAuthToken(authResponse.authToken)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepo", "Signup Error: ${e.message}")
            Result.failure(e)
        }
    }

    /**
     * Cierra la sesión borrando el token de la base de datos local.
     */
    suspend fun logout() = withContext(Dispatchers.IO) {
        localProgressRepository.clearAuthToken()
        Log.d("AuthRepo", "Logout completado. Token eliminado.")
    }
}