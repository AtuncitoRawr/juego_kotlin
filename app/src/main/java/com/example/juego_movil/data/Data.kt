package com.example.juego_movil.data

// Estructura de la solicitud de Login/Registro para Xano
data class AuthRequest(
    val email: String,
    val password: String,
    val username: String? = null
)

// Estructura de la respuesta de Xano tras Login/Registro
data class AuthResponse(
    val authToken: String,
    val user: User?
)

// Datos del usuario
data class User(
    val id: Int,
    val email: String,
    val username: String
)

// Estructura para guardar una nueva puntuación
data class SaveScoreRequest(
    val score: Int,
    val level: Int,
    val category: String
)