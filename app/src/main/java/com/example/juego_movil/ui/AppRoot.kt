//Aqui se maneja el menu como tal y las animaciones de fade

package com.example.juego_movil.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch


enum class Screen { MENU, GAME }

@Composable
fun AppRoot() {
    var screen by remember { mutableStateOf(Screen.MENU) }

    ImmersiveMode(enabled = true)
    when (screen) {
        Screen.MENU -> MainMenuScreen(onStart = { screen = Screen.GAME })
        Screen.GAME -> GameScreen(onExitToMenu = { screen = Screen.MENU })
    }
}

@Composable
fun MainMenuScreen(onStart: () -> Unit) {
    // Fade-in al entrar al menú
    val screenAlpha = remember { Animatable(1f) }
    val scope = rememberCoroutineScope()
    LaunchedEffect(Unit) {
        screenAlpha.snapTo(1f)
        screenAlpha.animateTo(0f, tween(200))
    }

    // Parpadeo del botón START
    val infinite = rememberInfiniteTransition(label = "startBlink")
    val blinkAlpha by infinite.animateFloat(
        initialValue = 0.1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 800, easing = FastOutLinearInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D1A))
            .graphicsLayer { alpha = 1f - screenAlpha.value } // 1→0 -> 0→1 visual
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Completos Journey",
                fontSize = 44.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )

            Box(modifier = Modifier.padding(top = 28.dp))

            Text(
                text = "START",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFEB3B),
                modifier = Modifier
                    .padding(horizontal = 24.dp, vertical = 8.dp)
                    .clickable {

                        scope.launch {
                            screenAlpha.animateTo(1f, tween(1000)) // fade-out
                            onStart()
                        }
                    }
                    .graphicsLayerAlpha(blinkAlpha)
            )
        }
    }
}

// Helper simple para aplicar alpha a un Modifier
private fun Modifier.graphicsLayerAlpha(alpha: Float): Modifier =
    this.graphicsLayer { this.alpha = alpha }
