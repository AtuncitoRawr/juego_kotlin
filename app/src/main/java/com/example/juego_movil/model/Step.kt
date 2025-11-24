package com.example.juego_movil.model

//private val GameState.checkpoints: Any
private const val CAM_LERP = 15f  // mayor = cámara más rápida
private const val DEATH_MARGIN = 200f // cuánto más abajo de la sala cuenta como vacío




fun step(game: GameState, dt: Float) {

    val p = game.player

    // SÓLIDOS = plataformas + fantasmas + zócalos de spikes
    val solids: List<Platform> =
        if (game.spikes.isEmpty()) game.platforms + game.ghostPlatforms
        else game.platforms + game.ghostPlatforms +
                game.spikes.map { Platform(spikeSupportRect(it)) }

    // --- INPUT ---
    p.moveDir = game.inputMoveDir.coerceIn(-1f, 1f)
    p.xspd = p.moveDir * p.moveSpd

    if (game.inputJumpPressedOnce && p.jumpCount < p.jumpMax) {
        if (isOnGround(p, game.platforms) || p.jumpCount < p.jumpMax) {
            p.yspd = p.jspd
            p.jumpCount++
        }
    }
    game.inputJumpPressedOnce = false

    // --- GRAVEDAD ---
    p.yspd = (p.yspd + p.grav * dt).coerceIn(-p.termVel, p.termVel)

    // --- MOVIMIENTO ---
    moveAndCollideX(p, solids, p.xspd * dt)
    val landed = moveAndCollideY(p, solids, p.yspd * dt)
    if (landed) p.jumpCount = 0

    if (p.moveDir > 0f) p.face = 1 else if (p.moveDir < 0f) p.face = -1

    if (p.x < 0f) { p.x = 0f; p.xspd = 0f }
    if (p.x + p.w > game.roomWidth) { p.x = game.roomWidth - p.w; p.xspd = 0f }

    // --- CAÍDA FUERA DEL MUNDO ---
    if (p.y > game.roomHeight + DEATH_MARGIN) {
        game.deaths++
        game.reset()
        return
    }

    // --- PINCHOS ---
    if (hitsSpike(p.rect(), game.spikes)) {
        game.deaths++
        game.reset()
        return
    }

    // ======================
    //   CHECKPOINT (C)
    // ======================
    game.checkpoints.forEachIndexed { index, cp ->
        if (p.rect().overlaps(cp)) {

            // Evitar reactivar el mismo checkpoint
            if (game.lastCheckpointIndex != index) {

                game.checkpointX = cp.left
                game.checkpointY = cp.top - 0.01f   // igual que el spawn

                game.hasCheckpoint = true
                game.lastCheckpointIndex = index
            }
        }
    }

    // =======================
    //  ENEMIGOS
    // =======================
    game.enemies.forEach { e ->

        if (!enemyGroundAhead(e, game.platforms) && isOnGround(e, game.platforms)) {
            e.moveDir *= -1f
        }

        e.xspd = e.moveDir * e.moveSpd

        val hitX = moveAndCollideX(e, solids, e.xspd * dt)
        if (hitX) e.moveDir *= -1f

        e.yspd = (e.yspd + e.grav * dt).coerceIn(-e.termVel, e.termVel)
        moveAndCollideY(e, solids, e.yspd * dt)

        if (e.x < 0f) { e.x = 0f; e.moveDir = 1f }
        if (e.x + e.w > game.roomWidth) { e.x = game.roomWidth - e.w; e.moveDir = -1f }

        if (p.rect().overlaps(e.rect())) {
            game.deaths++
            game.reset()
            return
        }
    }

    // =======================
    // PUERTAS
    // =======================
    val pRect = p.rect()

    // Puerta normal
    if (game.doors.any { d -> pRect.overlaps(d.rect) }) {
        game.nextLevelRequested = true
        return
    }

    // Puerta secreta
    if (game.secretDoors.any { d -> pRect.overlaps(d.rect) }) {
        game.currentLevel = 5
        game.nextLevelRequested = true
        game.secretDoorTriggered = true
        return
    }

    // =======================
    // CÁMARA
    // =======================
    val targetX = p.x + p.w / 2f - game.camWidth / 2f
    val targetY = p.y + p.h / 2f - game.camHeight / 2f

    game.camX += (targetX - game.camX) * CAM_LERP * dt
    game.camY += (targetY - game.camY) * CAM_LERP * dt

    game.camX = game.camX.coerceIn(0f, game.roomWidth - game.camWidth)
    game.camY = game.camY.coerceIn(0f, game.roomHeight - game.camHeight)

}
