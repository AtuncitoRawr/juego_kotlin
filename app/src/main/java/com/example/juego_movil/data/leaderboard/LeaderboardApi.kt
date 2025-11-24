package com.example.juego_movil.data.leaderboard

import com.example.juego_movil.data.SaveScoreRequest // Mantenemos la importación de SaveScoreRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface LeaderboardApi {

    // Obtiene lista de marcadores
    // para asegurar que Retrofit no use un tipo local.
    @GET("GET/scores")
    suspend fun getLeaderboard(
        @Query("level") level: Int?,
        @Query("category") category: String
    ): List<com.example.juego_movil.data.LeaderboardEntry> // <-- Tipo FQND (Fully Qualified Name)

    // Guarda una nueva puntuación
    @POST("POST_/score")
    suspend fun saveScore(@Body request: SaveScoreRequest): Unit
}