package com.example.juego_movil.ui

import android.app.Activity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Componente Composable para controlar el modo inmersivo (pantalla completa).
 * Oculta las barras de estado y navegación del sistema.
 * * @param enabled Si es 'true', activa el modo inmersivo. Si es 'false' (o al salir del Composable), lo desactiva.
 */
@Composable
fun ImmersiveMode(enabled: Boolean) {
    val activity = LocalContext.current as Activity

    DisposableEffect(enabled) {
        val window = activity.window
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        // Comportamiento: Las barras se pueden mostrar temporalmente con un gesto de deslizamiento.
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE

        if (enabled) {
            // Activar el modo inmersivo
            WindowCompat.setDecorFitsSystemWindows(window, false)
            controller.hide(WindowInsetsCompat.Type.systemBars())
        } else {
            // Desactivar el modo inmersivo (restaurar)
            controller.show(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }

        // Cleanup: Asegura que las barras se muestren al salir de este Composable.
        onDispose {
            controller.show(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, true)
        }
    }
}