package com.example.juego_movil.repository

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.juego_movil.model.AppSession
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object (DAO) para manejar la entidad AppSession.
 * Esta entidad está diseñada como un registro único (singleton) con id = 0
 * para almacenar el estado global (nivel desbloqueado, token de autenticación).
 */
@Dao
interface AppSessionDao {

    /**
     * Obtiene el registro único de la sesión de la aplicación (con ID 0).
     * Retorna Flow<AppSession?> para manejar el caso en que la tabla esté vacía.
     */
    @Query("SELECT * FROM app_session WHERE id = 0")
    fun getSession(): Flow<AppSession?>

    /**
     * Inserta o actualiza el registro de sesión.
     * Dado que el ID siempre es 0, OnConflictStrategy.REPLACE actúa como un UPSERT
     * para mantener un único registro de sesión.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateSession(session: AppSession)

    /**
     * Consulta específica para limpiar solo el token de autenticación (cierre de sesión).
     */
    @Query("UPDATE app_session SET authToken = NULL WHERE id = 0")
    suspend fun clearAuthToken()
}