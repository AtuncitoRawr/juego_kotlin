package com.example.juego_movil.viewmodel

import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import com.example.juego_movil.model.GameState
import com.example.juego_movil.repository.LevelRepository
import androidx.compose.ui.unit.IntSize
import android.content.Context
import androidx.lifecycle.viewModelScope
import com.example.juego_movil.repository.LocalProgressRepository
import kotlinx.coroutines.launch

class GameViewModel(
    private val repo: LevelRepository,
    private val progressRepo: LocalProgressRepository
) : ViewModel() {

    // Estado observable del juego
    val game: MutableState<GameState> = mutableStateOf(GameState())


    // ==================================================
    // =========== SISTEMA DE PROGRESO ==================
    // ==================================================

    /**
     * Carga el progreso inicial desde la DB.
     * Lo puedes llamar al abrir el selector de niveles si quieres.
     */
    suspend fun loadProgressInitial() {
        val unlocked = progressRepo.getHighestUnlockedLevel()

        game.value = game.value.apply {
            currentLevel = currentLevel.coerceAtMost(unlocked)
        }
    }

    /**
     * Desbloquea el siguiente nivel si corresponde.
     */
    fun saveProgressIfNeeded() {
        val levelJustCleared = game.value.currentLevel
        val nextToUnlock = levelJustCleared + 1


        viewModelScope.launch {
            val currentUnlocked = progressRepo.getHighestUnlockedLevel()
            if (nextToUnlock > currentUnlocked) {
                progressRepo.setHighestUnlockedLevel(nextToUnlock)
            }
        }
    }


    // ==================================================
    // === INICIALIZACIÓN Y MANEJO DE NIVELES ===========
    // ==================================================

    /** Carga el nivel actual según currentLevel. */
    fun startGame(context: Context, canvasSize: IntSize) {
        repo.create(context, game.value, canvasSize)
        game.value.startedAtNanos = System.nanoTime()
        game.value = game.value
    }

    fun advanceToNextLevel(context: Context, canvasSize: IntSize) {
        if (game.value.gameEnded) return

        val levelCleared = game.value.currentLevel
        val next = levelCleared + 1

        if (next <= game.value.finalLevel) {
            // guarda progreso SOLO UNA VEZ
            saveProgressIfNeeded()

            game.value.currentLevel = next
            game.value.nextLevelRequested = false
            repo.create(context, game.value, canvasSize)

        } else {
            // último nivel → END
            saveProgressIfNeeded()
            game.value.nextLevelRequested = false
            game.value.endRequested = true
        }

        game.value = game.value
    }


    /** Puerta secreta: salta a nivel especial. */
    fun jumpToSecretLevel(level: Int = 5) {
        game.value.currentLevel = level
        game.value.nextLevelRequested = true
        game.value = game.value
    }


    // ==================================================
    // ========== LÓGICA DE JUEGO POR FRAME =============
    // ==================================================
    fun step(dt: Float) {
        if (game.value.gameEnded) return

        com.example.juego_movil.model.step(game.value, dt)

        if (game.value.endRequested && !game.value.gameEnded) {
            game.value.gameEnded = true
            game.value.endElapsedMs =
                ((System.nanoTime() - game.value.startedAtNanos) / 1_000_000L)
        }

        game.value = game.value
    }


    // ==================================================
    // ================ CONTROLES ========================
    // ==================================================

    fun onMoveDir(dir: Float) {
        game.value.inputMoveDir = dir
        game.value = game.value
    }

    fun onJump() {
        game.value.inputJumpPressedOnce = true
        game.value = game.value
    }

    fun resetLevel() {
        game.value.reset()
        game.value = game.value
    }


    // ==================================================
    // ============ PUERTAS / FLUJOS INTERNOS ===========
    // ==================================================

    /*
    Es obsoleta pero la dejo porsiacaso la llegase a necesitar
    fun requestNextLevel() {
        if (game.value.currentLevel < game.value.finalLevel) {
            game.value.currentLevel += 1
            game.value.nextLevelRequested = true
        } else {
            game.value.endRequested = true
        }
        game.value = game.value
    }*/
}


