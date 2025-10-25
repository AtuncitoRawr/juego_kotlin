//Las funciones de colisiones tanto con solidos como con puas

package com.example.juego_movil.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.min

private const val STEP_MAX = 2f      // tamaño de paso del sweep en px
private const val SKIN = 0.5f        // “holgura” para no quedar clavado al borde
private const val MICRO = 0.1f       // micro-ajuste final

private fun collides(r: Rect, walls: List<Platform>): Boolean {
    for (w in walls) if (r.overlaps(w.rect)) return true
    return false
}

fun moveAndCollideX(p: Player, walls: List<Platform>, dx: Float): Boolean {
    if (dx == 0f) return false
    var remaining = dx
    var collided = false
    while (abs(remaining) > 0f) {
        val step = sign(remaining) * min(STEP_MAX, abs(remaining))
        val next = Rect(p.x + step, p.y, p.x + step + p.w, p.y + p.h)
        if (collides(next, walls)) {
            // micro-ajuste hacia el borde sin entrar en colisión
            var micro = sign(step) * MICRO
            while (!collides(Rect(p.x + micro, p.y, p.x + micro + p.w, p.y + p.h), walls)) {
                p.x += micro
            }
            // aplicar “skin” para despegar del borde
            p.x -= sign(step) * SKIN
            p.xspd = 0f
            collided = true
            break
        } else {
            p.x += step
            remaining -= step
        }
    }
    return collided
}

fun moveAndCollideY(p: Player, walls: List<Platform>, dy: Float): Boolean {
    if (dy == 0f) return false
    var remaining = dy
    var landed = false
    while (abs(remaining) > 0f) {
        val step = sign(remaining) * min(STEP_MAX, abs(remaining))
        val next = Rect(p.x, p.y + step, p.x + p.w, p.y + step + p.h)
        if (collides(next, walls)) {
            // micro-ajuste vertical
            var micro = sign(step) * MICRO
            while (!collides(Rect(p.x, p.y + micro, p.x + p.w, p.y + micro + p.h), walls)) {
                p.y += micro
            }
            // aplicar “skin”
            p.y -= sign(step) * SKIN
            p.yspd = 0f
            if (step > 0) landed = true // venía bajando → aterrizó
            break
        } else {
            p.y += step
            remaining -= step
        }
    }
    return landed
}

// Helper pequeño para checkear “en el suelo”
fun isOnGround(p: Player, walls: List<Platform>): Boolean {
    val test = Rect(p.x, p.y + SKIN + 1f, p.x + p.w, p.y + p.h + SKIN + 1f)
    return collides(test, walls)
}

private fun rectOf(e: Enemy) = androidx.compose.ui.geometry.Rect(e.x, e.y, e.x + e.w, e.y + e.h)

fun moveAndCollideX(e: Enemy, walls: List<Platform>, dx: Float): Boolean {
    if (dx == 0f) return false
    var remaining = kotlin.math.abs(dx)
    val dir = kotlin.math.sign(dx)
    var collided = false
    while (remaining > 0f) {
        val step = dir * kotlin.math.min(STEP_MAX, remaining)
        val next = androidx.compose.ui.geometry.Rect(e.x + step, e.y, e.x + step + e.w, e.y + e.h)
        if (collides(next, walls)) {
            var micro = dir * MICRO
            while (!collides(Rect(e.x + micro, e.y, e.x + micro + e.w, e.y + e.h), walls)) {
                e.x += micro
            }
            e.x -= dir * SKIN
            e.xspd = 0f
            collided = true
            break
        } else {
            e.x += step
            remaining -= kotlin.math.abs(step)
        }
    }
    return collided
}

fun moveAndCollideY(e: Enemy, walls: List<Platform>, dy: Float): Boolean {
    if (dy == 0f) return false
    var remaining = kotlin.math.abs(dy)
    val dir = kotlin.math.sign(dy)
    var landed = false
    while (remaining > 0f) {
        val step = dir * kotlin.math.min(STEP_MAX, remaining)
        val next = androidx.compose.ui.geometry.Rect(e.x, e.y + step, e.x + e.w, e.y + step + e.h)
        if (collides(next, walls)) {
            var micro = dir * MICRO
            while (!collides(Rect(e.x, e.y + micro, e.x + e.w, e.y + micro + e.h), walls)) {
                e.y += micro
            }
            e.y -= dir * SKIN
            e.yspd = 0f
            if (dir > 0) landed = true
            break
        } else {
            e.y += step
            remaining -= kotlin.math.abs(step)
        }
    }
    return landed
}

fun isOnGround(e: Enemy, walls: List<Platform>): Boolean {
    val test = androidx.compose.ui.geometry.Rect(e.x, e.y + SKIN + 1f, e.x + e.w, e.y + e.h + SKIN + 1f)
    return collides(test, walls)
}

// ✅ Ground “ahead” para patrulla (evita caer por bordes)
fun enemyGroundAhead(e: Enemy, walls: List<Platform>): Boolean {
    val aheadX = if (e.moveDir >= 0f) e.x + e.w + 2f else e.x - 2f
    val footY = e.y + e.h + 2f
    val test = Rect(aheadX, footY, aheadX + 1f, footY + 1f)
    return collides(test, walls)
}

fun spikeTriangle(s: Spike): Array<Offset> {
    val r = s.rect
    val tl = Offset(r.left,  r.top)
    val tr = Offset(r.right, r.top)
    val br = Offset(r.right, r.bottom)
    val bl = Offset(r.left,  r.bottom)
    return when (s.dir) {
        SpikeDir.UP    -> arrayOf(bl, br, Offset(r.center.x, r.top))
        SpikeDir.DOWN  -> arrayOf(tl, tr, Offset(r.center.x, r.bottom))
        SpikeDir.LEFT  -> arrayOf(tr, br, Offset(r.left,  r.center.y))
        SpikeDir.RIGHT -> arrayOf(tl, bl, Offset(r.right, r.center.y))
    }
}

fun pointInTriangle(p: Offset, a: Offset, b: Offset, c: Offset): Boolean {
    val b1 = sign2D(p, a, b) < 0f
    val b2 = sign2D(p, b, c) < 0f
    val b3 = sign2D(p, c, a) < 0f
    return (b1 == b2) && (b2 == b3)
}

fun sign2D(p1: Offset, p2: Offset, p3: Offset): Float {
    return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y)
}

fun spikeSupportRect(s: Spike): Rect {
    val r = s.rect
    val t = min(r.width, r.height) * 0.18f // grosor de la franja (~18% del tile); ajústalo si quieres
    return when (s.dir) {
        SpikeDir.UP    -> Rect(r.left,  r.bottom - t, r.right, r.bottom)   // franja en la base (abajo)
        SpikeDir.DOWN  -> Rect(r.left,  r.top,        r.right, r.top + t)  // franja arriba
        SpikeDir.LEFT  -> Rect(r.right - t, r.top,    r.right, r.bottom)   // franja a la derecha
        SpikeDir.RIGHT -> Rect(r.left,     r.top,     r.left + t, r.bottom)// franja a la izquierda
    }
}

fun hitsSpike(playerRect: Rect, spikes: List<Spike>): Boolean {
    for (s in spikes) {
        if (!playerRect.overlaps(s.rect)) continue

        val tri = spikeTriangle(s)
        val corners = arrayOf(
            Offset(playerRect.left,  playerRect.top),
            Offset(playerRect.right, playerRect.top),
            Offset(playerRect.right, playerRect.bottom),
            Offset(playerRect.left,  playerRect.bottom)
        )
        if (corners.any { pointInTriangle(it, tri[0], tri[1], tri[2]) }) return true

        val center = Offset(playerRect.center.x, playerRect.center.y)
        if (pointInTriangle(center, tri[0], tri[1], tri[2])) return true
    }
    return false
}
