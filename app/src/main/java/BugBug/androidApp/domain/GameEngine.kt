package BugBug.androidApp.domain


import BugBug.androidApp.model.Insect
import BugBug.androidApp.model.InsectType
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import kotlin.math.sqrt
import kotlin.random.Random

object GameEngine {

    fun spawnInsects(
        count: Int,
        fieldSize: Size,
        difficulty: Int
    ): List<Insect> {
        val speed = 1f + difficulty * 0.5f
        val insectSize = 120f
        return List(count) { index ->
            Insect(
                id = System.currentTimeMillis() + index,
                type = InsectType.values().random(),
                position = Offset(
                    x = Random.nextFloat() * (fieldSize.width - insectSize),
                    y = Random.nextFloat() * (fieldSize.height - insectSize)
                ),
                velocity = Offset(
                    x = (Random.nextFloat() - 0.5f) * speed * 4f,
                    y = (Random.nextFloat() - 0.5f) * speed * 4f
                ),
                size = Size(insectSize, insectSize)
            )
        }
    }

    fun moveInsects(
        insects: List<Insect>,
        fieldSize: Size
    ): List<Insect> = insects.map { insect ->
        var newX = insect.position.x + insect.velocity.x
        var newY = insect.position.y + insect.velocity.y
        var vx = insect.velocity.x
        var vy = insect.velocity.y

        if (newX < 0f || newX + insect.size.width > fieldSize.width) {
            vx = -vx
            newX = newX.coerceIn(0f, fieldSize.width - insect.size.width)
        }
        if (newY < 0f || newY + insect.size.height > fieldSize.height) {
            vy = -vy
            newY = newY.coerceIn(0f, fieldSize.height - insect.size.height)
        }

        insect.copy(
            position = Offset(newX, newY),
            velocity = Offset(vx, vy)
        )
    }

    fun isHit(insect: Insect, tap: Offset): Boolean {
        val dx = tap.x - (insect.position.x + insect.size.width / 2f)
        val dy = tap.y - (insect.position.y + insect.size.height / 2f)
        val radius = insect.size.width / 2f
        return sqrt(dx * dx + dy * dy) <= radius
    }
}
