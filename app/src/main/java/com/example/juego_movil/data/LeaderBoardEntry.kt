package com.example.juego_movil.data

// Modelo de datos para una entrada del marcador
// (id, user_name, score)
data class LeaderboardEntry(
    // ID del registro
    val id: Int,

    // Nombre de usuario
    val username: String,

    // Puntuación, que puede ser tiempo o muertes
    val score: Int
)