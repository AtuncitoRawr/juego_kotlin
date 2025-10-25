package com.example.juego_movil

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.Surface
import com.example.juego_movil.ui.AppRoot
import com.example.juego_movil.ui.theme.Juego_movilTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        requestedOrientation = android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            Juego_movilTheme {
                Surface {
                    AppRoot()
                }
            }
        }
    }
}

