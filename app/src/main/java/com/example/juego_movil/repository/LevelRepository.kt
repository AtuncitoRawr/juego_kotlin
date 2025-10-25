package com.example.juego_movil.repository

import android.content.Context
import androidx.compose.ui.unit.IntSize
import com.example.juego_movil.model.GameState
import kotlin.math.max

class LevelRepository {
// Lee un archivo ASCII de /app/src/main/assets/
    /** Lee el txt de assets y retorna el contenido. */
    fun loadLevelFromAssets(context: Context, fileName: String): String {
        return context.assets.open(fileName).bufferedReader().use { it.readText() }
    }

    /**
     * Crea/Inicializa el nivel leyendo un ASCII desde assets (opción B).
     * - Asegúrate de tener /app/src/main/assets/level1.txt
     * - La cámara se ajusta al canvas recibido
     * - Se setean plataformas, enemigos y spawn del player
     */
    fun create(context: Context, game: GameState, canvasSizePx: IntSize) {
        //if (ended) return // no recargar nada si ya estamos en la pantalla final

        // Tamaño de cámara = tamaño del canvas actual
        // 🔹 Cámara más pequeña (zoom)
        // después de obtener canvasSizePx
        game.zoom = 1f // ej. zoom-in 2x
        // ✅ viewport lógico = canvas / zoom (en float)
        game.camWidth = canvasSizePx.width.toFloat() / game.zoom
        game.camHeight = canvasSizePx.height.toFloat() / game.zoom


        // Tamaño lógico por tile (ajústalo según tu arte)
        val tile = 64f

        val levelFile = "Level${game.currentLevel}.txt"
        val raw = runCatching { loadLevelFromAssets(context, levelFile) }
            .onFailure { android.util.Log.e("LEVEL", "Missing/failed asset: $levelFile", it) }
            .getOrElse { err ->
                if (game.currentLevel >= game.finalLevel) {
                    game.endRequested = true
                    return                      // ⬅️ no sigas creando nada
                } else {
                    throw err                   // otros niveles: deja ver el error
                }
            }

        val ascii = normalizeAscii(raw)


        // Parsear ASCII → LevelData (usa tu LevelLoader.parseAsciiLevel)
        val level = parseAsciiLevel(ascii, tile)

        // Dimensiones del mundo (sala) según el nivel
        // Garantizamos al menos el tamaño de la cámara para evitar clamps extraños
        game.roomWidth = max(level.roomWidth, game.camWidth)
        game.roomHeight = max(level.roomHeight, game.camHeight)

        // Cargar plataformas
        game.platforms.clear()
        game.platforms.addAll(level.platforms)

        // Cargar enemigos (manteniendo su spawn para reset)
        game.enemies.clear()
        game.enemies.addAll(level.enemies)

        // ⬇️ Cargar pinchos
        game.spikes.clear()
        game.spikes.addAll(level.spikes)

        //Cargar puertas
        game.doors.clear()
        game.doors.addAll(level.doors)

        game.secretDoors.clear();
        game.secretDoors.addAll(level.secretDoors)


        game.ghostPlatforms.clear();
        game.ghostPlatforms.addAll(level.ghostPlatforms)


        // Posicionar jugador en el spawn del nivel
        val p = game.player
        p.spawnX = level.playerSpawnX
        p.spawnY = level.playerSpawnY - p.h // sobre la celda

        game.reset()
        // Reset de dinámicas del player (sin tocar el nivel)
        p.xspd = 0f
        p.yspd = 0f
        p.jumpCount = 0
        p.face = 1

        // Centrar cámara en el player y limitar a la sala
        val targetCamX = p.x + p.w / 2f - game.camWidth / 2f
        val targetCamY = p.y + p.h / 2f - game.camHeight / 2f
        game.camX = targetCamX.coerceIn(0f, game.roomWidth - game.camWidth)
        game.camY = targetCamY.coerceIn(0f, game.roomHeight - game.camHeight)


        android.util.Log.d("LEVEL", "Enemies: " + game.enemies.joinToString {
            "(${it.x}, ${it.y}) spawn=(${it.spawnX},${it.spawnY})"
        })

    }
}
