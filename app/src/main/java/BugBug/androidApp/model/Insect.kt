package BugBug.androidApp.model

import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size

enum class InsectType(val title: String, val points: Int, val spawnWeight: Int) {
    BEETLE("Божья коровка", 5, 35),
    FLY("Муха", 10, 20),
    BUG("Клоп", 20, 20),
    WASP("Оса", 15, 10),
    RHINO("Жук-носорог", 25, 8),
    CATERPILLAR("Гусеница", 10, 7),
    CENTIPEDE("Сороконожка", 0, 0),
    ARMORED_BEETLE("Жук-щит", 0, 0),
    SPIDER("Паук", 0, 0),
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