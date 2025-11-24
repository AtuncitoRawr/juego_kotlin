package com.example.juego_movil.repository

import android.content.Context
import androidx.compose.ui.unit.IntSize
import com.example.juego_movil.model.GameState
import kotlin.math.max

class LevelRepository {

    fun loadLevelFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    fun create(context: Context, game: GameState, canvasSizePx: IntSize) {

        game.zoom = 1f
        game.camWidth = canvasSizePx.width.toFloat() / game.zoom
        game.camHeight = canvasSizePx.height.toFloat() / game.zoom

        val tile = 64f
        val levelFile = "Level${game.currentLevel}.txt"

        val raw = runCatching { loadLevelFromAssets(context, levelFile) }
            .onFailure { android.util.Log.e("LEVEL", "Missing/failed asset: $levelFile", it) }
            .getOrElse { err ->
                if (game.currentLevel >= game.finalLevel) {
                    game.endRequested = true
                    return
                } else throw err
            }

        val ascii = normalizeAscii(raw)
        val level = parseAsciiLevel(ascii, tile)

        // =========================
        // CARGAR PLATAFORMAS/ENTIDADES
        // =========================
        game.platforms.clear();       game.platforms.addAll(level.platforms)
        game.ghostPlatforms.clear();  game.ghostPlatforms.addAll(level.ghostPlatforms)
        game.enemies.clear();         game.enemies.addAll(level.enemies)
        game.spikes.clear();          game.spikes.addAll(level.spikes)
        game.doors.clear();           game.doors.addAll(level.doors)
        game.secretDoors.clear();     game.secretDoors.addAll(level.secretDoors)

        // =========================
        // CHECKPOINTS
        // =========================
        game.checkpoints = level.checkpoints
        game.clearCheckpoint()       // <-- limpiar cualquier checkpoint previo

        // =========================
        // ROOM SIZE
        // =========================
        game.roomWidth = max(level.roomWidth, game.camWidth)
        game.roomHeight = max(level.roomHeight, game.camHeight)

        // =========================
        // PLAYER SPAWN
        // =========================
        val p = game.player
        p.spawnX = level.playerSpawnX
        p.spawnY = level.playerSpawnY - p.h

        game.reset()

        // =========================
        // CÁMARA INICIAL
        // =========================
        val targetCamX = p.x + p.w / 2f - game.camWidth / 2f
        val targetCamY = p.y + p.h / 2f - game.camHeight / 2f

        game.camX = targetCamX.coerceIn(0f, game.roomWidth - game.camWidth)
        game.camY = targetCamY.coerceIn(0f, game.roomHeight - game.camHeight)

        android.util.Log.d("LEVEL", "Enemies: " + game.enemies.joinToString {
            "(${it.x}, ${it.y}) spawn=(${it.spawnX},${it.spawnY})"
        })
    }
}
