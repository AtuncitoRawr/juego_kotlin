package com.example.juego_movil.data.leaderboard

data class LeaderboardEntry(
    val userId: Int,
    val username: String,
    val level: Int,
    val time: Float,
    val deaths: Int,
    val totalTime: Float,
    val totalDeaths: Int
)

enum class LeaderboardMode {
    LEVEL,
    GLOBAL
}

enum class LeaderboardCategory {
    TIME,
    DEATHS
}
