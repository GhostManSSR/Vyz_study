package BugBug.androidApp.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

enum class InsectType(val title: String, val points: Int) {
    BEETLE("Божья коровка", 5),
    FLY("Жук", 10),
    BUG("Клоп", 20)
}

data class Insect(
    val id: Long,
    val type: InsectType,
    val position: Offset,
    val velocity: Offset,
    val size: Size,
    val isAlive: Boolean = true
)