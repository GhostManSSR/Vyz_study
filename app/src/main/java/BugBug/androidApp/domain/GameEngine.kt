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
        difficulty: Int,
        speedMultiplier: Float = 1.0f
    ): List<Insect> {
        val baseSpeed = 2f + difficulty * 0.8f
        val insectSize = 120f
        val margin = 50f

        return List(count) { index ->
            val type = randomType()
            Insect(
                id = System.currentTimeMillis() + index,
                type = type,
                position = Offset(
                    x = Random.nextFloat() * (fieldSize.width - insectSize - margin * 2) + margin,
                    y = Random.nextFloat() * (fieldSize.height - insectSize - margin * 2) + margin
                ),
                velocity = Offset(
                    x = (Random.nextFloat() - 0.5f) * baseSpeed * speedMultiplier * 3f,
                    y = (Random.nextFloat() - 0.5f) * baseSpeed * speedMultiplier * 3f
                ),
                size = Size(insectSize, insectSize)
            )
        }
    }

    fun moveInsects(
        insects: List<Insect>,
        fieldSize: Size,
        speedMultiplier: Float = 1.0f
    ): List<Insect> = insects.map { insect ->
        var newX = insect.position.x + insect.velocity.x * speedMultiplier
        var newY = insect.position.y + insect.velocity.y * speedMultiplier
        var vx = insect.velocity.x
        var vy = insect.velocity.y

        if (newX < 0f) {
            vx = -vx
            newX = 0f
        } else if (newX + insect.size.width > fieldSize.width) {
            vx = -vx
            newX = fieldSize.width - insect.size.width
        }

        if (newY < 0f) {
            vy = -vy
            newY = 0f
        } else if (newY + insect.size.height > fieldSize.height) {
            vy = -vy
            newY = fieldSize.height - insect.size.height
        }

        if (Random.nextFloat() < 0.02f) {
            vx = (vx + (Random.nextFloat() - 0.5f) * 2f).coerceIn(-15f, 15f)
            vy = (vy + (Random.nextFloat() - 0.5f) * 2f).coerceIn(-15f, 15f)
        }

        insect.copy(
            position = Offset(newX, newY),
            velocity = Offset(vx, vy)
        )
    }

    fun isHit(insect: Insect, tap: Offset, hitRadius: Float? = null): Boolean {
        val radius = hitRadius ?: insect.size.width / 2f
        val centerX = insect.position.x + insect.size.width / 2f
        val centerY = insect.position.y + insect.size.height / 2f

        val dx = tap.x - centerX
        val dy = tap.y - centerY
        val distance = sqrt(dx * dx + dy * dy)

        return distance <= radius
    }

    fun isHitCircle(position: Offset, tap: Offset, radius: Float): Boolean {
        val dx = tap.x - position.x
        val dy = tap.y - position.y
        val distance = sqrt(dx * dx + dy * dy)
        return distance <= radius
    }

    fun limitInsects(
        insects: List<Insect>,
        maxInsects: Int,
        priorityTypes: List<InsectType> = emptyList()
    ): List<Insect> {
        if (insects.size <= maxInsects) return insects

        return insects
            .sortedWith(
                compareBy<Insect> { it.id }
                    .thenBy { if (it.type in priorityTypes) 1 else 0 }
            )
            .takeLast(maxInsects)
    }

    fun selectInsectsToRemove(
        insects: List<Insect>,
        maxInsects: Int
    ): List<Insect> {
        if (insects.size <= maxInsects) return emptyList()

        val toRemove = insects.size - maxInsects
        return insects
            .sortedBy { it.id }
            .take(toRemove)
    }

    fun randomPosition(
        fieldSize: Size,
        insectSize: Size,
        margin: Float = 50f
    ): Offset {
        return Offset(
            x = Random.nextFloat() * (fieldSize.width - insectSize.width - margin * 2) + margin,
            y = Random.nextFloat() * (fieldSize.height - insectSize.height - margin * 2) + margin
        )
    }

    fun spawnSingleInsect(
        fieldSize: Size,
        difficulty: Int,
        speedMultiplier: Float = 1.0f
    ): Insect {
        val type = randomType()
        val baseSpeed = 2f + difficulty * 0.8f
        val insectSize = 120f

        return Insect(
            id = System.currentTimeMillis(),
            type = type,
            position = randomPosition(fieldSize, Size(insectSize, insectSize)),
            velocity = Offset(
                x = (Random.nextFloat() - 0.5f) * baseSpeed * speedMultiplier * 3f,
                y = (Random.nextFloat() - 0.5f) * baseSpeed * speedMultiplier * 3f
            ),
            size = Size(insectSize, insectSize)
        )
    }

    fun spawnGoldenInsect(fieldSize: Size, difficulty: Int): Insect {
        val baseSpeed = 2f + difficulty * 0.8f
        val insectSize = 110f
        val margin = 50f

        return Insect(
            id = System.currentTimeMillis() + 999,
            type = InsectType.GOLDEN,
            position = Offset(
                x = Random.nextFloat() * (fieldSize.width - insectSize - margin * 2) + margin,
                y = Random.nextFloat() * (fieldSize.height - insectSize - margin * 2) + margin
            ),
            velocity = Offset(
                x = (Random.nextFloat() - 0.5f) * baseSpeed * 3f,
                y = (Random.nextFloat() - 0.5f) * baseSpeed * 3f
            ),
            size = Size(insectSize, insectSize)
        )
    }

    private fun randomType(): InsectType {
        val weighted = InsectType.values().filter { it.spawnWeight > 0 }
        if (weighted.isEmpty()) return InsectType.BEETLE

        val total = weighted.sumOf { it.spawnWeight }
        var r = Random.nextInt(total)
        for (type in weighted) {
            r -= type.spawnWeight
            if (r < 0) return type
        }
        return InsectType.BEETLE
    }
    fun moveInsectsWithGravity(
        insects: List<Insect>,
        fieldSize: Size,
        tiltX: Float,
        tiltY: Float,
        speedMultiplier: Float = 1.0f
    ): List<Insect> {
        val gravityStrength = 0.6f * speedMultiplier

        return insects.map { insect ->
            var vx = insect.velocity.x + tiltX * gravityStrength
            var vy = insect.velocity.y + tiltY * gravityStrength

            vx *= 0.97f
            vy *= 0.97f

            var newX = insect.position.x + vx * speedMultiplier
            var newY = insect.position.y + vy * speedMultiplier

            if (newX < 0f) { vx = -vx * 0.8f; newX = 0f }
            if (newX + insect.size.width > fieldSize.width) {
                vx = -vx * 0.8f
                newX = fieldSize.width - insect.size.width
            }
            if (newY < 0f) { vy = -vy * 0.8f; newY = 0f }
            if (newY + insect.size.height > fieldSize.height) {
                vy = -vy * 0.8f
                newY = fieldSize.height - insect.size.height
            }

            insect.copy(
                position = Offset(newX, newY),
                velocity = Offset(vx, vy)
            )
        }
    }
}