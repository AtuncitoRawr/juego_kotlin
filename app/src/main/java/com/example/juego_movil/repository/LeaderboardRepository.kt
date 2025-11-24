package com.example.juego_movil.repository

import com.example.juego_movil.data.LeaderboardEntry
import com.example.juego_movil.data.leaderboard.LeaderboardApi
import com.example.juego_movil.data.leaderboard.LeaderboardCategory
import com.example.juego_movil.data.leaderboard.LeaderboardMode
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Interfaz para la lógica de carga de marcadores.
 */
interface LeaderboardRepository {
    suspend fun loadLeaderboard(
        mode: LeaderboardMode,
        category: LeaderboardCategory,
        level: Int
    ): Result<List<LeaderboardEntry>>
}

/**
 * Repositorio que se conecta a la API de Xano a través de LeaderboardApi
 * para obtener los datos reales del marcador.
 */
class RealLeaderboardRepository(private val api: LeaderboardApi) : LeaderboardRepository {
    override suspend fun loadLeaderboard(
        mode: LeaderboardMode,
        category: LeaderboardCategory,
        level: Int
    ): Result<List<LeaderboardEntry>> = withContext(Dispatchers.IO) {
        try {
            // 1. Determinar los parámetros de la API
            // La API espera la categoría como String (ej. "TIME" o "DEATHS")
            val apiCategory = category.name

            // La API espera 'level' como Int? (null para modo global)
            // Si el modo es GLOBAL, enviamos null para cargar el marcador sin filtro de nivel.
            val apiLevel = if (mode == LeaderboardMode.LEVEL) level else null

            // 2. Llamada a la API
            val data = api.getLeaderboard(
                level = apiLevel,
                category = apiCategory
            )

            // 3. Devolvemos la lista de LeaderboardEntry obtenida
            Result.success(data)
        } catch (e: Exception) {
            // Captura errores de red, JSON o HTTP
            println("Error al cargar marcadores desde la API: ${e.message}")
            Result.failure(e)
        }
    }
}