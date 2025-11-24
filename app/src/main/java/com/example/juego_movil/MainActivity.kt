package com.example.juego_movil

import android.content.pm.ActivityInfo
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.example.juego_movil.ui.AppRoot
import com.example.juego_movil.ui.theme.Juego_movilTheme
import com.google.accompanist.systemuicontroller.rememberSystemUiController

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        //FORZAR LA ORIENTACIÓN A HORIZONTAL (LANDSCAPE)
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE


        setContent {
            Juego_movilTheme {
                val systemUiController = rememberSystemUiController()
                val useDarkIcons = !isSystemInDarkTheme()

                SideEffect {
                    // Configuración inicial de barras (transparente).
                    // AppRoot luego aplicará el ImmersiveMode.
                    systemUiController.setSystemBarsColor(
                        color = Color.Transparent,
                        darkIcons = useDarkIcons
                    )
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // AppRoot maneja toda la lógica de estado, autenticación y navegación.
                    AppRoot()
                }
            }
        }
    }
}
