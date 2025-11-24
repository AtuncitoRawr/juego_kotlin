package com.example.juego_movil.repository

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.juego_movil.model.AppSession

/**
 * Definición de la base de datos Room.
 * Versión 3: Necesaria para incluir el campo 'authToken' en AppSession.
 */
@Database(entities = [AppSession::class], version = 3, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun sessionDao(): AppSessionDao
}