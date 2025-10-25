package com.example.juego_movil.ui


import android.view.KeyEvent.KEYCODE_DPAD_LEFT
import android.view.KeyEvent.KEYCODE_DPAD_RIGHT
import android.view.KeyEvent.KEYCODE_R
import android.view.KeyEvent.KEYCODE_SPACE
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.juego_movil.viewmodel.GameViewModel
import com.example.juego_movil.viewmodel.GameVMFactory
import com.example.juego_movil.repository.LevelRepository


import androidx.compose.foundation.Canvas
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.android.awaitFrame

// Teclado y foco
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.FocusRequester
import android.view.KeyEvent as AndroidKeyEvent

// Material (icono Home)
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.ui.input.key.type
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.juego_movil.model.GameState
import com.example.juego_movil.step
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch





@Composable
fun GameScreen(onExitToMenu: () -> Unit = {}) {

    // Tiempo/muertes → UI fin de juego
    var showEnd by remember { mutableStateOf(false) }
    var ended by remember { mutableStateOf(false) }
    var lastEnded by remember { mutableStateOf(false) }

    var showTime by remember { mutableStateOf(false) }
    var showDeaths by remember { mutableStateOf(false) }
    var showMenuBtn by remember { mutableStateOf(false) }
    val endFade = remember { Animatable(0f) }  // overlay fade

    val scope = rememberCoroutineScope()
    val fade = remember { Animatable(0f) } // 0 = sin fade, 1 = pantalla negra
    val context = LocalContext.current
    val vm: GameViewModel = viewModel(factory = GameVMFactory(LevelRepository()))
    val game by vm.game

    var created by remember { mutableStateOf(false) }
    var canvasSize by remember { mutableStateOf(IntSize(0, 0)) }

    var frameTick by remember { mutableStateOf(0L) }

    // teclado
    val focusRequester = remember { FocusRequester() }
    LaunchedEffect(Unit) { focusRequester.requestFocus() }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusable(true)
            .onKeyEvent { event ->
                val code = event.nativeKeyEvent.keyCode
                when (event.type) {
                    KeyEventType.KeyDown -> {
                        when (code) {
                            KEYCODE_DPAD_LEFT  -> { vm.onMoveDir(-1f); true }
                            KEYCODE_DPAD_RIGHT -> { vm.onMoveDir( 1f); true }
                            KEYCODE_SPACE      -> { vm.onJump(); true }
                            KEYCODE_R          -> { vm.resetLevel(); true }

                            else -> false
                        }
                    }
                    KeyEventType.KeyUp -> {
                        when (code) {
                            KEYCODE_DPAD_LEFT, KEYCODE_DPAD_RIGHT -> { vm.onMoveDir(0f); true }
                            else -> false
                        }
                    }
                    else -> false
                }
            }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .onSizeChanged { size ->
                canvasSize = size
                if (!created && size.width > 0 && size.height > 0) {

                    // 🔧 HARD RESET (evita volver directo al END al iniciar desde el menú)
                    vm.game.value.apply {
                        gameEnded = false
                        endRequested = false
                        nextLevelRequested = false
                        secretDoorTriggered = false

                        // contadores de la partida
                        deaths = 0
                        endElapsedMs = 0

                        // nivel inicial
                        currentLevel = 1   // <-- si tu partida empieza en otro, cámbialo aquí

                        // tiempo de inicio
                        startedAtNanos = System.nanoTime()
                    }

                    // Limpia flags locales del overlay (UI)
                    ended = false
                    showEnd = false
                    showTime = false
                    showDeaths = false
                    showMenuBtn = false

                    // Crea/carga el nivel normalmente
                    vm.startGame(context, size)
                    created = true
                }
            }

        ) {
            frameTick.hashCode()

            if (!game.gameEnded) {
                drawGame(this, game)
            }
        }

        if (!ended) {

            // Controles táctiles
            MovementControls(
                onMoveDir = { dir -> vm.onMoveDir(dir) },
                onJumpPressed = { vm.onJump() }


            )

            // 🔙 Botón Home casi invisible (arriba-derecha)
            IconButton(
                onClick = {
                    scope.launch {
                        fade.animateTo(1f, tween(1000))
                        onExitToMenu()
                    }
                },
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(horizontal = 16.dp, vertical = 16.dp)
                    .size(36.dp)
                    .alpha(0.3f) // casi invisible
            ) {
                Icon(
                    imageVector = Icons.Filled.Home,
                    contentDescription = "Volver al menú",
                    tint = Color.White
                )
            }
        } else {
            // Fondo negro sólido (sin depender de endFade)
            Canvas(
                modifier = Modifier.fillMaxSize()
            ) {
                drawRect(Color.Black) // ← sólido
            }

            // Contenido END encima
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Mostrar END inmediatamente (ya lo activaste con showEnd = true)
                if (showEnd) {
                    Text("END", color = Color.White, fontSize = 48.sp, fontWeight = FontWeight.ExtraBold)
                }
                if (showTime) {
                    val secs = (game.endElapsedMs / 1000).toInt()
                    val ms = (game.endElapsedMs % 1000).toInt()
                    Text("Tiempo: ${secs}s ${ms}ms", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(top = 12.dp))
                }
                if (showDeaths) {
                    Text("Muertes: ${game.deaths}", color = Color.White, fontSize = 20.sp, modifier = Modifier.padding(top = 6.dp))
                }
                if (showMenuBtn) {
                    Text(
                        text = "MENU",
                        color = Color(0xFFFFEB3B),
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .padding(top = 20.dp)
                            .clickable {
                                // limpia flags locales y vuelve al menú
                                showEnd = false; showTime = false; showDeaths = false; showMenuBtn = false
                                ended = false
                                onExitToMenu()
                            }
                    )
                }
            }
        }


    }
    // Overlay de fade (negro)
    if (fade.value > 0f) {
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            drawRect(Color.Black.copy(alpha = fade.value))
        }
    }


    LaunchedEffect(Unit) {
        if (game.startedAtNanos == 0L) game.startedAtNanos = System.nanoTime()

        var lastTime = 0L


        while (true) {
            val frameTime = awaitFrame()
            if (lastTime == 0L) { lastTime = frameTime; continue }

            val dtSec = ((frameTime - lastTime) / 1_000_000_000.0).toFloat()
            lastTime = frameTime
            val dt = kotlin.math.min(dtSec, 1f / 30f)

            // 1) si ya terminó, no hagas step (pero el loop sigue vivo para la UI)
            if (!game.gameEnded) {
                try {
                    vm.step(dt)

                } catch (t: Throwable) {
                    android.util.Log.e("LEVEL", "Crash inside step()", t)
                    game.endRequested = true
                }
            }

            if (game.gameEnded && !lastEnded) {
                lastEnded = true
                ended = true
                showEnd = true
                scope.launch {
                    delay(1000); showTime = true
                    delay(1000); showDeaths = true
                    delay(3000); showMenuBtn = true
                }
            }

            if (game.secretDoorTriggered && !game.gameEnded) {
                game.secretDoorTriggered = false
                vm.jumpToSecretLevel(5)
                created = false
                vm.startGame(context, canvasSize)
            }




            if (game.nextLevelRequested && !game.gameEnded) {
                // ⬇️ Delega TODO a la VM (subir nivel + crear)
                vm.advanceToNextLevel(context, canvasSize)
            }



            frameTick++
        }
    }
}
