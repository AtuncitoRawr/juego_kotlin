package com.example.juego_movil.repository

import android.content.Context
import androidx.room.Room
import com.example.juego_movil.model.AppSession
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

/**
 * Repositorio para gestionar el progreso local del jugador (nivel desbloqueado más alto)
 * y el estado de la sesión (token de autenticación).
 *
 * Utiliza AppSessionDao para interactuar con la base de datos Room.
 */
class LocalProgressRepository(private val context: Context) {

    private val db: AppDatabase by lazy {
        Room.databaseBuilder(
            context.applicationContext,
            AppDatabase::class.java,
            "juego_movil_db"
        )

            .fallbackToDestructiveMigration()
            .build()
    }

    private val sessionDao: AppSessionDao = db.sessionDao()

    /**
     * Función interna para obtener la sesión existente, o inicializar una nueva
     * si la base de datos está vacía. Previene NullPointerException.
     */
    private suspend fun getSessionOrInitialize(): AppSession {
        // Obtenemos la sesión existente (puede ser nula si la tabla está vacía)
        val existingSession = sessionDao.getSession().first()

        // Si la sesión es nula, creamos e insertamos la sesión por defecto.
        return if (existingSession == null) {
            val defaultSession = AppSession(
                id = 0, // Siempre 0 para el registro único de sesión
                highestUnlockedLevel = 1,
                authToken = null
            )
            sessionDao.updateSession(defaultSession)
            defaultSession
        } else {
            existingSession
        }
    }


    /**
     * Obtiene el nivel desbloqueado más alto.
     */
    suspend fun getHighestUnlockedLevel(): Int {
        return getSessionOrInitialize().highestUnlockedLevel
    }

    /**
     * Establece el nuevo nivel desbloqueado más alto.
     */
    suspend fun setHighestUnlockedLevel(level: Int) {
        val currentSession = getSessionOrInitialize()

        // Usamos .copy() para mantener el token si existe
        val updatedSession = currentSession.copy(
            highestUnlockedLevel = level
        )
        sessionDao.updateSession(updatedSession)
    }

    /**
     * Cuenta el número de archivos de nivel disponibles en los assets.
     */
    fun countLevels(): Int {
        val assetManager = context.assets
        val files = assetManager.list("") ?: return 0
        return files.count { it.startsWith("Level") && it.endsWith(".txt") }
    }

    // --- Métodos de Autenticación ---

    /**
     * Guarda el token de autenticación en la sesión local.
     */
    suspend fun saveAuthToken(token: String?) {
        val currentSession = getSessionOrInitialize()

        // Usamos .copy() para mantener el nivel desbloqueado
        val updatedSession = currentSession.copy(
            authToken = token
        )
        sessionDao.updateSession(updatedSession)
    }

    /**
     * Obtiene el token de autenticación actual como un Flow.
     */
    fun getAuthTokenFlow(): Flow<String?> {
        return sessionDao.getSession().map { session -> session?.authToken }
    }

    /**
     * Limpia el token de autenticación (Cierre de Sesión Local).
     */
    suspend fun clearAuthToken() {
        saveAuthToken(null)
    }
}