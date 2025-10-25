package com.example.juego_movil

import com.example.juego_movil.model.GameState
import com.example.juego_movil.model.PatrolType
import com.example.juego_movil.model.Platform
import com.example.juego_movil.model.enemyGroundAhead
import com.example.juego_movil.model.hitsSpike
import com.example.juego_movil.model.isOnGround
import com.example.juego_movil.model.moveAndCollideX
import com.example.juego_movil.model.moveAndCollideY
import com.example.juego_movil.model.spikeSupportRect

private const val CAM_LERP = 15f  // mayor = cámara más rápida
private const val DEATH_MARGIN = 200f // cuánto más abajo de la sala cuenta como vacío




fun step(game: GameState, dt: Float) {

    val p = game.player

    // Sólidos = plataformas + (pinchos como sólidos rectangulares)
    // Sólidos = plataformas + “zócalos” de pinchos (no todo el AABB)
    val solids: List<Platform> =
        if (game.spikes.isEmpty()) game.platforms + game.ghostPlatforms
        else game.platforms + game.ghostPlatforms + game.spikes.map { Platform(spikeSupportRect(it)) }




    // 1) Input → velocidad horizontal (px/seg)
    p.moveDir = game.inputMoveDir.coerceIn(-1f, 1f)
    p.xspd = p.moveDir * p.moveSpd

    // 2) Salto solo si tiene saltos disponibles o está en el suelo
    if (game.inputJumpPressedOnce && p.jumpCount < p.jumpMax) {
        // Permite saltar si está en el suelo o tiene saltos aéreos restantes
        if (isOnGround(p, game.platforms) || p.jumpCount < p.jumpMax) {
            p.yspd = p.jspd
            p.jumpCount++
        }
    }
    game.inputJumpPressedOnce = false

    // 3) Gravedad (aceleración)


    if (p.yspd < p.termVel)
    {
        p.yspd += p.grav * dt
    } else {p.yspd = p.termVel}

    // 4) Movimiento + colisión (sweep)
    // horizontal primero → luego vertical (estilo plataforma clásico)
    moveAndCollideX(p, solids, p.xspd * dt)
    val landed = moveAndCollideY(p, solids, p.yspd * dt)
    if (landed) p.jumpCount = 0
    // 5) Cara
    if (p.moveDir > 0f) p.face = 1 else if (p.moveDir < 0f) p.face = -1

    // 6) Límites de sala en X (mantener para no salir por los lados)
    if (p.x < 0f) { p.x = 0f; p.xspd = 0f }
    if (p.x + p.w > game.roomWidth) { p.x = game.roomWidth - p.w; p.xspd = 0f }

// 6b) Death plane: si cae por debajo de la sala, reinicia el nivel
    if (p.y > game.roomHeight + DEATH_MARGIN) {
        game.deaths++
        game.reset()
        return
    }

    if (hitsSpike(p.rect(), game.spikes)) {
        game.deaths++
        game.reset()
        return
    }


    // ⬇️ Tras mover al player y actualizar cámara, agrega:
    game.enemies.forEach { e ->
        // AI simple: si no hay suelo adelante, gira
        if (!enemyGroundAhead(e, game.platforms) && isOnGround(e, game.platforms)) {
            e.moveDir *= -1f
        }

        // Velocidad horizontal por patrulla
        e.xspd = e.moveDir * e.moveSpd

        // Movimiento X + giro si choca lateralmente
        val hitX = moveAndCollideX(e, solids, e.xspd * dt)
        if (hitX) e.moveDir *= -1f

        // Gravedad + clamp de velocidad terminal
        e.yspd = (e.yspd + e.grav * dt).coerceIn(-e.termVel, e.termVel)
        moveAndCollideY(e, solids, e.yspd * dt)

        // ——— GIRO POR RANGO / TIEMPO ———
        when (e.patrolType) {
            PatrolType.RANGE -> {
                if (e.useMaxDistance) {
                    val dx = e.x - e.spawnX
                    if (dx > e.maxDistanceFromSpawn) { e.x = e.spawnX + e.maxDistanceFromSpawn; e.moveDir = -1f }
                    if (dx < -e.maxDistanceFromSpawn) { e.x = e.spawnX - e.maxDistanceFromSpawn; e.moveDir =  1f }
                } else {
                    // límites absolutos
                    if (e.x < e.patrolLeft)  { e.x = e.patrolLeft;  e.moveDir = 1f }
                    if (e.x + e.w > e.patrolRight) { e.x = e.patrolRight - e.w; e.moveDir = -1f }
                }
            }
            PatrolType.TIMER -> {
                /*
                e.patrolClock += dt
                if (e.patrolClock >= e.patrolFlipEvery) {
                    e.patrolClock = 0f
                    e.moveDir *= -1f
                }*/
            }
        }

// Seguridad extra: rebote en límites del mundo
        if (e.x < 0f) { e.x = 0f; e.moveDir = 1f }
        if (e.x + e.w > game.roomWidth) { e.x = game.roomWidth - e.w; e.moveDir = -1f }



        // Cara
        if (e.moveDir > 0f) e.face = 1 else if (e.moveDir < 0f) e.face = -1

        // 🔥 Colisión con player → reset nivel
        val p = game.player
        val overlap =
            p.x < e.x + e.w && p.x + p.w > e.x &&
                    p.y < e.y + e.h && p.y + p.h > e.y

        if (overlap) {
            game.deaths++
            game.reset()  // resetea player y enemigos a sus spawn
            return@forEach
        }
    }

    // Puerta amarilla → siguiente nivel o FIN si ya estás en el último
    // Puerta amarilla → siguiente nivel o FIN si estás en el último
    val pRect = p.rect()
    if (game.doors.any { d -> pRect.overlaps(d.rect) }) {
        // ⬅️ NO incrementes currentLevel aquí
        // ⬅️ NO pongas endRequested aquí
        game.nextLevelRequested = true
        return
    }


    // 🔸 Puerta secreta 'F' → salta directamente al nivel 5
    if (game.secretDoors.any { door -> pRect.overlaps(door.rect) }) {
        game.currentLevel = 5              // ⬅️ salto directo
        game.nextLevelRequested = true
        game.secretDoorTriggered = true
        return
    }



    // 🔹 Actualizar la cámara para que siga al player
// centrado en unidades del mundo
    val targetX = p.x + p.w/2f - game.camWidth /2f
    val targetY = p.y + p.h/2f - game.camHeight/2f

// si usas lerp:
    game.camX += (targetX - game.camX) * CAM_LERP * dt
    game.camY += (targetY - game.camY) * CAM_LERP * dt

// límites del mundo usando viewport lógico
    game.camX = game.camX.coerceIn(0f, game.roomWidth  - game.camWidth)
    game.camY = game.camY.coerceIn(0f, game.roomHeight - game.camHeight)

}
