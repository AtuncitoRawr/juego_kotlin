package com.example.juego_movil.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.juego_movil.model.GameState
import com.example.juego_movil.repository.LevelRepository
import androidx.compose.ui.unit.IntSize
import android.content.Context

class GameViewModel(
    private val repo: LevelRepository
) : ViewModel() {

    // Estado central observable por la UI
    val game: MutableState<GameState> = mutableStateOf(GameState())

    // Progreso y métricas
    var currentLevel: Int
        get() = game.value.currentLevel
        set(value) { game.value.currentLevel = value }



    // === API que usará la UI ===

    /** Se llama al entrar al juego o cuando cambia el tamaño del canvas. */
    fun startGame(context: Context, canvasSize: IntSize) {
        // Marca inicio si procede, y crea/carga el nivel
        if (game.value.startedAtNanos == 0L) game.value.startedAtNanos = System.nanoTime()
        repo.create(context, game.value, canvasSize) // ← usa tu create() ya existente
        // Notificar cambios (opcional si modificas referencias profundas)
        game.value = game.value
    }

    /** Bucle por frame: avanza la simulación dt segundos. */
    fun step(dt: Float) {
        if (game.value.gameEnded) return
        // Llama tu lógica de frame (mueve al Player/Enemigos, colisión, cámara, etc.)
        // Antes tenías una función top-level step(game, dt) → ahora simplemente invócala:
        com.example.juego_movil.step(game.value, dt)

        // Gestiona señales de cambio de nivel o fin:
        if (game.value.endRequested && !game.value.gameEnded) {
            // Sella fin
            game.value.endRequested = false
            game.value.gameEnded = true
            game.value.endElapsedMs = ((System.nanoTime() - game.value.startedAtNanos) / 1_000_000L)
        }

        // Notificar (opcional si mutaste campos del mismo objeto)
        game.value = game.value
    }

    /** Input: mover izquierda/derecha (-1..1). */
    fun onMoveDir(dir: Float) {
        game.value.inputMoveDir = dir.coerceIn(-1f, 1f)
        game.value = game.value
    }

    /** Input: salto (one-shot). */
    fun onJump() {
        game.value.inputJumpPressedOnce = true
        game.value = game.value
    }

    /** Reset del nivel (por muerte o tecla R). */
    fun resetLevel() {
        game.value.reset()
        game.value = game.value
    }

    /** Llamar cuando se confirma tocar puerta normal. */
    fun requestNextLevel() {
        if (game.value.currentLevel < game.value.finalLevel) {
            game.value.currentLevel += 1
            game.value.nextLevelRequested = true
        } else {
            game.value.endRequested = true
        }
        game.value = game.value
    }

    /** Puerta secreta “F” → Ir directo al nivel 5 (o el que definas). */
    fun jumpToSecretLevel(level: Int = 5) {
        game.value.currentLevel = level
        game.value.nextLevelRequested = true
        game.value = game.value
    }

    // NUEVO en GameViewModel.kt
    fun advanceToNextLevel(context: Context, canvasSize: IntSize) {
        // evita repetir si ya terminó
        if (game.value.gameEnded) return

        if (game.value.currentLevel < game.value.finalLevel) {
            game.value.currentLevel += 1
            game.value.nextLevelRequested = false
            // vuelve a crear/cargar el nuevo nivel usando el repo
            repo.create(context, game.value, canvasSize)
        } else {
            // si ya era el último, fin del juego
            game.value.nextLevelRequested = false
            game.value.endRequested = true
        }
        // notifica cambios
        game.value = game.value
    }


}
