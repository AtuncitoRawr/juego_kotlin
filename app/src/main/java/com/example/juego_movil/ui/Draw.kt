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

    drawRect(Color(0xFF0D0D1A))

    withTransform({
        translate(-game.camX, -game.camY)
        scale(game.zoom)
    }) {

        // ============================================
        // 1. PLATAFORMAS
        // ============================================
        game.platforms.forEach {
            drawRect(
                color = Color(0xFF2E7D32),
                topLeft = Offset(it.rect.left, it.rect.top),
                size = Size(it.rect.width, it.rect.height)
            )
        }


        // ============================================
// CHECKPOINTS CON BRILLO PULSANTE
// ============================================
        game.checkpoints.forEach { cp ->
            val cx = cp.left + cp.width / 2f
            val cy = cp.top + cp.height / 2f

            // radio base del círculo
            val baseRadius = cp.width * 0.38f

            // animación pulsante: 0.85 → 1.15
            val pulse = (1f + kotlin.math.sin((System.currentTimeMillis() % 1000) / 1000f * 6.28f) * 0.15f)

            val radius = baseRadius * pulse

            // brillo exterior suave
            drawCircle(
                color = Color(0x884CAF50),
                radius = radius * 1.7f,
                center = Offset(cx, cy)
            )

            // cuerpo principal
            drawCircle(
                color = Color(0xFF4CAF50),
                radius = radius,
                center = Offset(cx, cy)
            )
        }


        // ============================================
        // 3. PLAYER
        // ============================================
        val p = game.player
        drawRect(
            color = Color(0xFF42A5F5),
            topLeft = Offset(p.x, p.y),
            size = Size(p.w, p.h)
        )

        // ============================================
        // 4. ENEMIGOS
        // ============================================
        game.enemies.forEach { e ->
            drawRect(
                color = Color(0xFFE53935),
                topLeft = Offset(e.x, e.y),
                size = Size(e.w, e.h)
            )
        }

        // ============================================
        // 5. SPIKES (TRIÁNGULOS)
        // ============================================
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

        // ============================================
        // 6. PUERTAS NORMALES
        // ============================================
        game.doors.forEach { d ->
            drawRect(
                color = Color.Yellow,
                topLeft = Offset(d.rect.left, d.rect.top),
                size = Size(d.rect.width, d.rect.height)
            )
        }

        // ============================================
        // 7. PUERTAS SECRETAS
        // ============================================
        game.secretDoors.forEach { d ->
            drawRect(
                color = Color(0xFFFFB300),
                topLeft = Offset(d.rect.left, d.rect.top),
                size = Size(d.rect.width, d.rect.height)
            )
        }

        // ============================================
        // 8. PLATAFORMAS FANTASMA
        // ============================================
        game.ghostPlatforms.forEach {
            drawRect(
                color = Color(0xFF1A1A2E).copy(alpha = 0.06f),
                topLeft = Offset(it.rect.left, it.rect.top),
                size = Size(it.rect.width, it.rect.height)
            )
        }
    }
}
