package com.example.juego_movil.ui

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.withTransform
import com.example.juego_movil.model.GameState
import com.example.juego_movil.model.spikeTriangle

fun drawGame(drawScope: DrawScope, game: GameState) = with(drawScope) {
    // fondo
    drawRect(Color(0xFF0D0D1A))

    // cámara + zoom
    withTransform({
        translate(-game.camX, -game.camY) // en unidades del mundo
        scale(game.zoom)                  // zoom
    }) {
        // plataformas
        game.platforms.forEach {
            drawRect(
                color = Color(0xFF2E7D32),
                topLeft = Offset(it.rect.left, it.rect.top),
                size = Size(it.rect.width, it.rect.height)
            )
        }

        // player
        val p = game.player
        drawRect(
            color = Color(0xFF42A5F5),
            topLeft = Offset(p.x, p.y),
            size = Size(p.w, p.h)
        )

        // enemigos
        game.enemies.forEach { e ->
            drawRect(
                color = Color(0xFFE53935),
                topLeft = Offset(e.x, e.y),
                size = Size(e.w, e.h)
            )
        }


        game.spikes.forEach { s ->
            val tri = spikeTriangle(s)
            val path = Path().apply {
                moveTo(tri[0].x, tri[0].y)
                lineTo(tri[1].x, tri[1].y)
                lineTo(tri[2].x, tri[2].y)
                close()
            }
            drawPath(path = path, color = Color(0xFFEEEEEE))
        }

        game.doors.forEach { d ->
            drawRect(
                color = Color.Yellow,
                topLeft = Offset(d.rect.left, d.rect.top),
                size = Size(d.rect.width, d.rect.height)
            )
        }

        game.secretDoors.forEach { d ->
            drawRect(
                color = Color(0xFFFFB300),
                topLeft = Offset(d.rect.left, d.rect.top),
                size = Size(d.rect.width, d.rect.height)
            )
        }

        // Fantasmas (semi invisibles)
        game.ghostPlatforms.forEach {
            drawRect(
                color = Color(0xFF1A1A2E).copy(alpha = 0.06f),
                topLeft = Offset(it.rect.left, it.rect.top),
                size = Size(it.rect.width, it.rect.height)
            )
        }


    }
}


