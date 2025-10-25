package com.example.juego_movil.model

import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.geometry.Rect

data class Player(
    var x: Float = 0f,
    var y: Float = 0f,
    var w: Float = 64f,
    var h: Float = 64f,

    // Velocidades en px/seg
    var xspd: Float = 0f,
    var yspd: Float = 0f,

    // Input → -1..1
    var moveDir: Float = 0f,

    // Config “sensación plataforma”
    var moveSpd: Float = 550f,   // px/seg
    var jspd: Float = -700f,     // salto inicial (px/seg)
    var grav: Float = 1600f,     // gravedad (px/seg^2)
    val termVel:Float = 1600f,

    var face: Int = 1,

    var hpMax: Int = 100,
    var hp: Int = 100,

    var jumpCount: Int = 0,
    var jumpMax: Int = 1,

    var spawnX: Float = Float.NaN,
    var spawnY: Float = Float.NaN
) {
    init {
        if (spawnX.isNaN()) spawnX = x
        if (spawnY.isNaN()) spawnY = y
    }

    fun rect(): Rect = Rect(x, y, x + w, y + h)
}

// ⬇️ Agrega esto (o extiende tu Enemy para incluir los spawn)
data class Enemy (
    var x: Float = 0f,
    var y: Float = 0f,
    var w: Float = 64f,
    var h: Float = 64f,
    // Velocidades en px/seg
    var xspd: Float = 0f,
    var yspd: Float = 0f,
    // -1..1
    var moveDir: Float = -1f,
    // Sensación plataforma
    var moveSpd: Float = 140f,
    var grav: Float = 1300f,
    val termVel: Float = 1600f,
    var face: Int = -1,
    var patrolType: PatrolType = PatrolType.RANGE,
    // RANGE
    var patrolLeft: Float = 0f,
    var patrolRight: Float = 0f,
    var useMaxDistance: Boolean = false,   // si prefieres distancia en vez de límites absolutos
    var maxDistanceFromSpawn: Float = 300f,
// TIMER
    var patrolFlipEvery: Float = 2.5f,     // segundos
    var patrolClock: Float = 0f,
    // 🔹 Spawns para reset:
    var spawnX: Float = Float.NaN,
    var spawnY: Float = Float.NaN
)

{
    init {
        if (spawnX.isNaN()) spawnX = x
        if (spawnY.isNaN()) spawnY = y

        // ✅ Si usan RANGE pero no definiste límites, crea uno centrado en el spawn
        if (patrolType == PatrolType.RANGE && patrolLeft == 0f && patrolRight == 0f) {
            patrolLeft = spawnX - maxDistanceFromSpawn
            patrolRight = spawnX + maxDistanceFromSpawn
        }
    }
    fun rect(): Rect = Rect(x, y, x + w, y + h)
}

data class Platform(val rect: Rect)


data class Spike(
    val rect: androidx.compose.ui.geometry.Rect,
    val dir: SpikeDir
)

data class Door(val rect: Rect)


class GameState {
    var roomWidth by mutableStateOf(1280f)
    var roomHeight by mutableStateOf(720f)
    val player = Player()
    val enemies = mutableListOf<Enemy>()
    val platforms = mutableListOf<Platform>()
    val spikes = mutableListOf<Spike>()
    // Inputs
    var inputMoveDir: Float = 0f
    var inputJumpPressedOnce: Boolean = false
    // Camara
    var camX by mutableStateOf(0f)
    var camY by mutableStateOf(0f)
    var camWidth = 1280f
    var camHeight = 720f
    // dentro de GameState
    var zoom by mutableStateOf(1f) // 1f = normal, 2f = acercar (zoom-in), 0.75f = alejar (zoom-out)

    val doors = mutableListOf<Door>()          // ⬅️ puertas del nivel
    val secretDoors = mutableListOf<Door>()
    var secretDoorTriggered: Boolean = false
    val ghostPlatforms = mutableListOf<Platform>()
    var endRequested: Boolean = false
    var currentLevel: Int = 1               // índice actual (1 = Level1.txt)
    var finalLevel: Int = 4
    var gameEnded: Boolean = false

    var startedAtNanos: Long = 0L    // se setea al entrar a GameScreen
    var endElapsedMs: Long = 0L      // tiempo total al terminar
    var deaths: Int = 0
    var nextLevelRequested: Boolean = false    // bandera para pedir cargar siguiente




    fun reset() {
        // ❗ No limpiar plataformas aquí: el nivel ya está creado en create()
        player.x = player.spawnX
        player.y = player.spawnY
        player.xspd = 0f
        player.yspd = 0f
        player.moveDir = 0f
        player.moveSpd = 550f
        player.jspd = -700f
        player.grav = 1600f
        player.face = 1
        player.hpMax = 100
        player.hp = player.hpMax
        player.jumpCount = 0
        player.jumpMax = 1

        enemies.forEach { e ->
            e.x = e.spawnX
            e.y = e.spawnY
            e.patrolClock = 0f
            e.xspd = 0f
            e.yspd = 0f
            e.moveDir = if (e.face >= 0) 1f else -1f
        }
    }
}
