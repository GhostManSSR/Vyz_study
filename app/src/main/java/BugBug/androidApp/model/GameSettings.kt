package BugBug.androidApp.model

data class GameSettings(
    val speed: Float = 1.0f,
    val maxCockroaches: Int = 10,
    val bonusIntervalSec: Int = 30,
    val roundDurationSec: Int = 60
)
