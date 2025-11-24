package com.example.juego_movil.repository

import androidx.compose.ui.geometry.Rect
import com.example.juego_movil.model.Door
import com.example.juego_movil.model.Enemy
import com.example.juego_movil.model.LevelData
import com.example.juego_movil.model.PatrolType
import com.example.juego_movil.model.Platform
import com.example.juego_movil.model.Spike
import com.example.juego_movil.model.SpikeDir

// Caracteres considerados como sólidos
private val SOLIDS = setOf('=', '#', '*')

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
    val checkpoints = mutableListOf<Rect>()

    val consumed = mutableListOf<Pair<Int, Int>>()

    var playerX = 0f
    var playerY = 0f
    var playerFound = false

    for (r in 0 until rows) {
        val row = lines[r].padEnd(cols, ' ')
        var c = 0

        while (c < cols) {
            if (consumed.contains(r to c)) {
                c++
                continue
            }

            val ch = row[c]

            when {

                // ░ PLATFORMAS (unión horizontal automática)
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

                // ░ PLAYER
                ch == 'P' -> {
                    playerX = c * tile
                    playerY = (r * tile) - 0.01f
                    playerFound = true
                    c++
                }

                // ░ ENEMIGO
                ch == 'E' -> {
                    val ex = c * tile
                    val ey = r * tile

                    enemies += Enemy(
                        x = ex,
                        y = ey,
                        spawnX = ex,
                        spawnY = ey,
                        face = -1,
                        moveDir = -1f
                    ).apply {
                        patrolType = PatrolType.TIMER
                        patrolFlipEvery = 2.5f
                    }

                    c++
                }

                // ░ PUERTA NORMAL
                ch == 'D' -> {
                    val sx = c * tile
                    val sy = r * tile

                    val heightTiles = if (r + 1 < rows) 2 else 1
                    val doorH = heightTiles * tile

                    doors += Door(Rect(sx, sy, sx + tile, sy + doorH))

                    if (heightTiles == 2) consumed += ((r + 1) to c)
                    c++
                }

                // ░ PUERTA SECRETA
                ch == 'F' -> {
                    val sx = c * tile
                    val sy = r * tile

                    val heightTiles = if (r + 1 < rows) 2 else 1
                    val doorH = heightTiles * tile

                    secretDoors += Door(Rect(sx, sy, sx + tile, sy + doorH))

                    if (heightTiles == 2) consumed += ((r + 1) to c)
                    c++
                }

                // ░ CHECKPOINT
                ch == 'C' -> {
                    val cx = c * tile
                    val cy = r * tile
                    checkpoints += Rect(cx, cy, cx + tile, cy + tile)
                    c++
                }

                // ░ PINCHOS (direccionales)
                ch in listOf('1', '2', '3', '4') -> {
                    val sx = c * tile
                    val sy = r * tile

                    val dir = when (ch) {
                        '1' -> SpikeDir.LEFT
                        '2' -> SpikeDir.RIGHT
                        '3' -> SpikeDir.UP
                        else -> SpikeDir.DOWN
                    }

                    spikes += Spike(Rect(sx, sy, sx + tile, sy + tile), dir)
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
        ghostPlatforms = ghostPlatforms,
        enemies = enemies,
        spikes = spikes,
        doors = doors,
        secretDoors = secretDoors,
        checkpoints = checkpoints,
        playerSpawnX = playerX,
        playerSpawnY = playerY,
        roomWidth = roomW,
        roomHeight = roomH
    )
}

fun normalizeAscii(input: String, tabWidth: Int = 4): String {
    val expanded = input.replace("\t", " ".repeat(tabWidth))

    val lines = expanded.lines()
    val nonEmpty = lines.filter { it.trim().isNotEmpty() }
    val minIndent = nonEmpty.minOfOrNull {
        it.indexOfFirst { ch -> ch != ' ' }.takeIf { i -> i >= 0 } ?: 0
    } ?: 0

    return lines.joinToString("\n") { line ->
        if (line.length >= minIndent) line.drop(minIndent) else line.trimStart(' ')
    }
}
