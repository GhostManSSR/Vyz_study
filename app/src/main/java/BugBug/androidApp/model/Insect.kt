package BugBug.androidApp.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

enum class InsectType(val title: String, val points: Int, val spawnWeight: Int ) {
    BEETLE("Божья коровка", 5, 50),
    FLY("Жук", 10, 30),
    BUG("Клоп", 20, 20),
    GOLDEN("Золотой жук", 0, 0)
}

data class Insect(
    val id: Long,
    val type: InsectType,
    val position: Offset,
    val velocity: Offset,
    val size: Size,
    val isAlive: Boolean = true
)