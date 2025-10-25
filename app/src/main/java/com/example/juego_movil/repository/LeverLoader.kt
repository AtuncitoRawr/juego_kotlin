package com.example.juego_movil.repository

import androidx.compose.ui.geometry.Rect
import com.example.juego_movil.model.Door
import com.example.juego_movil.model.Enemy
import com.example.juego_movil.model.LevelData
import com.example.juego_movil.model.PatrolType
import com.example.juego_movil.model.Platform
import com.example.juego_movil.model.Spike
import com.example.juego_movil.model.SpikeDir

private val SOLIDS = setOf('=', '#', '*') // puedes ampliar

fun parseAsciiLevel(ascii: String, tile: Float): LevelData {
    val lines = ascii.trimEnd('\n', '\r').lines()
    require(lines.isNotEmpty()) { "Nivel vacío" }

    val rows = lines.size
    val cols = lines.maxOf { it.length }

    val platforms = mutableListOf<Platform>()
    val enemies = mutableListOf<Enemy>()
    val spikes = mutableListOf<Spike>()
    val doors = mutableListOf<Door>()
    val secretDoors = mutableListOf<Door>()
    val ghostPlatforms = mutableListOf<Platform>()
    val consumed = mutableListOf<Pair<Int,Int>>()

    var playerX = 0f
    var playerY = 0f
    var playerFound = false

    // Recorremos fila por fila, fusionando plataformas horizontales
    for (r in 0 until rows) {
        val row = lines[r].padEnd(cols, ' ')
        var c = 0
        while (c < cols) {
            if (consumed.contains(r to c)) {c++; continue}
            val ch = row[c]
            when {
                ch in SOLIDS -> {
                    val start = c
                    var end = c
                    while (end < cols && row[end] in SOLIDS) end++
                    val x = start * tile
                    val y = r * tile
                    val w = (end - start) * tile
                    val h = if (ch == '#') tile else tile * 0.5f
                    val yPos = if (ch == '#') y else (y + tile - h)
                    val rect = Rect(x, yPos, x + w, yPos + h)

                    if (ch == '*') ghostPlatforms += Platform(rect)
                    else platforms += Platform(rect)
                    c = end
                }

                ch == 'P' -> {
                    // Spawn del player
                    playerX = c * tile
                    playerY = (r * tile) - 0.01f
                    playerFound = true
                    c++
                }

                ch == 'E' -> {
                    val ex = c * tile
                    val ey = r * tile
                    enemies += Enemy(
                        x = ex,
                        y = ey,
                        face = -1,
                        moveDir = -1f,
                        spawnX = ex,
                        spawnY = ey
                    ).also {
                        // Evita RANGE sin límites: usa TIMER por defecto
                        it.patrolType = PatrolType.TIMER
                        it.patrolFlipEvery = 2.5f
                        // (Si prefieres RANGE automático, comenta lo de arriba y
                        //  deja que el init cree el rango alrededor del spawn)
                    }
                    c++
                }

                ch == 'D' -> {

                    val sx = c * tile
                    val sy = r * tile

                    // Puerta 1×2: intentamos tomar dos celdas verticales (esta y la de abajo)
                    val heightTiles = if (r + 1 < rows) 2 else 1
                    val doorH = heightTiles * tile
                    doors += Door(Rect(sx, sy, sx + tile, sy + doorH))

                    // Marcamos la celda inferior (si existe) como consumida para no crear otra puerta
                    if (heightTiles == 2) consumed += ((r + 1) to c)

                    c++
                }

                ch == 'F' -> {
                    val sx = c * tile
                    val sy = r * tile
                    val heightTiles = if (r + 1 < rows) 2 else 1
                    val doorH = heightTiles * tile
                    secretDoors += Door(Rect(sx, sy, sx + tile, sy + doorH))
                    if (heightTiles == 2) consumed += ((r + 1) to c)
                    c++
                }


                (ch == '1' || ch == '2' || ch == '3' || ch == '4') -> { // ⬅️ paréntesis arreglado
                    val sx = c * tile
                    val sy = r * tile
                    val sRect = Rect(sx, sy, sx + tile, sy + tile)
                    val dir = when (ch) {
                        '1' -> SpikeDir.LEFT
                        '2' -> SpikeDir.RIGHT
                        '3' -> SpikeDir.UP
                        else -> SpikeDir.DOWN
                    }
                    spikes += Spike(sRect, dir)
                    c++
                }

                else -> c++
            }
        }
    }

    val roomW = cols * tile
    val roomH = rows * tile

    if (!playerFound) {
        playerX = 0f
        playerY = 0f
    }

    return LevelData(
        platforms = platforms,
        enemies = enemies,
        spikes = spikes,
        doors = doors,
        playerSpawnX = playerX,
        playerSpawnY = playerY,
        roomWidth = roomW,
        roomHeight = roomH
    ).apply {
        // ⬅️ agrega la lista opcional de puertas secretas
        this.secretDoors = secretDoors
        this.ghostPlatforms = ghostPlatforms
    }
}


fun normalizeAscii(input: String, tabWidth: Int = 4): String {
    // 1) Reemplaza tabs por espacios
    val expanded = input.replace("\t", " ".repeat(tabWidth))

    // 2) Detecta la sangría mínima común (leading spaces) y la quita de todas las líneas no vacías
    val lines = expanded.lines()
    val nonEmpty = lines.filter { it.trim().isNotEmpty() }
    val minIndent = nonEmpty.minOfOrNull { it.indexOfFirst { ch -> ch != ' ' }.takeIf { i -> i >= 0 } ?: 0 } ?: 0
    return lines.joinToString("\n") { line ->
        if (line.length >= minIndent) line.drop(minIndent) else line.trimStart(' ')
    }
}
