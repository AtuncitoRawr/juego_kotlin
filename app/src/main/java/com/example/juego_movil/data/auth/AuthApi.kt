package com.example.juego_movil.data.auth

import com.example.juego_movil.data.AuthRequest
import com.example.juego_movil.data.AuthResponse
import com.example.juego_movil.data.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.GET

/**
 * Interfaz de Retrofit para el grupo de API de autenticación de Xano.
 */
interface AuthApi {

    // 1. Endpoint para registrar un nuevo usuario
    @POST("auth/signup")
    suspend fun signup(@Body request: AuthRequest): Response<AuthResponse>

    // 2. Endpoint para iniciar sesión
    @POST("auth/login")
    suspend fun login(@Body request: AuthRequest): Response<AuthResponse>

    // 3. Endpoint para obtener la información del usuario logueado
    @GET("auth/me")
    suspend fun getCurrentUser(): User
}