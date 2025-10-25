package com.example.juego_movil.model

data class LevelData(
    val platforms: List<Platform>,
    var ghostPlatforms: List<Platform> = emptyList(),
    val enemies: List<Enemy>,
    val spikes: List<Spike>,
    val doors: List<Door>,
    var secretDoors: List<Door> = emptyList(),
    val playerSpawnX: Float,
    val playerSpawnY: Float,
    val roomWidth: Float,
    val roomHeight: Float
)

enum class PatrolType { RANGE, TIMER }
enum class SpikeDir { LEFT, RIGHT, UP, DOWN }

