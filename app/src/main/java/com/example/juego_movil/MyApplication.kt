package com.example.juego_movil

import android.app.Application
import android.util.Log
import com.example.juego_movil.data.auth.AuthApi
import com.example.juego_movil.repository.AuthRepository
import com.example.juego_movil.repository.LocalProgressRepository // ✅ Usamos este repositorio
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// --- 1. CONFIGURACIÓN DE URL DE XANO ---
private const val BASE_DOMAIN_URL = "https://x8ki-letl-twmt.n7.xano.io/"
private const val AUTH_GROUP_PATH = "api:IHYvoOXu/"
private const val AUTH_BASE_URL = BASE_DOMAIN_URL + AUTH_GROUP_PATH

class MyApplication : Application() {

    // Instancia del Repositorio (Singleton que persistirá)
    lateinit var authRepository: AuthRepository
        private set

    // Instancia de Retrofit para el API
    private lateinit var retrofit: Retrofit

    // Función de ayuda para obtener la interfaz del API.
    private fun getAuthApi(): AuthApi {
        if (!::retrofit.isInitialized) {
            retrofit = Retrofit.Builder()
                .baseUrl(AUTH_BASE_URL) // Usamos la URL completa para Auth
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        return retrofit.create(AuthApi::class.java)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d("MyApplication", "Inicializando la clase Application y los Singletons.")


        // Este repositorio ya contiene la lógica para leer/escribir el token en la DB.
        val localProgressRepository = LocalProgressRepository(applicationContext)

        // Ahora le inyectamos el localProgressRepository en lugar del TokenRepository.
        authRepository = AuthRepository(
            api = getAuthApi(),
            localProgressRepository = localProgressRepository // ✅ Inyección correcta
        )
    }

    companion object {
        // Método de acceso al Singleton desde ViewModels/Activities.
        fun getAuthRepository(application: Application): AuthRepository {
            return (application as MyApplication).authRepository
        }
    }
}