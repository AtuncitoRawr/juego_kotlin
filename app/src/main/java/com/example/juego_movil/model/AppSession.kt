package com.example.juego_movil.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room para almacenar el estado global de la aplicación.
 */
@Entity(tableName = "app_session")
data class AppSession(
    @PrimaryKey val id: Int = 0,

    // Progreso del juego
    val highestUnlockedLevel: Int = 1,

    // Autenticación (Persistencia del Login)
    val authToken: String? = null,

    // Datos del Usuario (Para no tener que pedirlos a la API al reiniciar)
    val userId: Int = 0,
    val username: String = "",
    val email: String = ""
)