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

        return List(count) { index ->
            val type = randomType(difficulty)
            val insectSize = sizeFor(type, fieldSize)
            Insect(
                id = System.currentTimeMillis() + index,
                type = type,
                position = randomPosition(fieldSize, insectSize),
                velocity = Offset(
                    x = (Random.nextFloat() - 0.5f) * baseSpeed * speedMultiplier * 3f,
                    y = (Random.nextFloat() - 0.5f) * baseSpeed * speedMultiplier * 3f
                ),
                size = insectSize
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
        if (insect.type == InsectType.CENTIPEDE && hitRadius == null) {
            val drawnHeight = insect.size.height * 0.72f
            val drawnTop = (insect.size.height - drawnHeight) / 2f
            val centerY = insect.position.y + drawnTop + drawnHeight / 2f
            val segments = 9

            return (0 until segments).any { segment ->
                val x = insect.position.x +
                    insect.size.width * (0.1f + 0.8f * segment / (segments - 1f))
                val radius = maxOf(drawnHeight * if (segment == segments - 1) 0.18f else 0.16f, 30f)
                val dx = tap.x - x
                val dy = tap.y - centerY
                dx * dx + dy * dy <= radius * radius
            }
        }

        val radius = hitRadius ?: minOf(insect.size.width, insect.size.height) / 2f
        val centerX = insect.position.x + insect.size.width / 2f
        val centerY = insect.position.y + insect.size.height / 2f

        val dx = tap.x - centerX
        val dy = tap.y - centerY
        val distance = sqrt(dx * dx + dy * dy)

        return distance <= radius
    }

    fun splitInsect(insect: Insect, fieldSize: Size, firstId: Long): List<Insect> =
        when (insect.type) {
            InsectType.CENTIPEDE -> splitIntoTwo(insect, fieldSize, firstId, InsectType.CATERPILLAR)
            InsectType.ARMORED_BEETLE -> splitIntoTwo(insect, fieldSize, firstId, InsectType.BUG)
            InsectType.SPIDER -> splitIntoTwo(insect, fieldSize, firstId, InsectType.FLY)
            else -> emptyList()
        }

    private fun splitIntoTwo(
        insect: Insect,
        fieldSize: Size,
        firstId: Long,
        childType: InsectType
    ): List<Insect> {
        val childSize = sizeFor(childType, fieldSize)
        val centerY = insect.position.y + insect.size.height / 2f
        val maxX = (fieldSize.width - childSize.width).coerceAtLeast(0f)
        val maxY = (fieldSize.height - childSize.height).coerceAtLeast(0f)
        val childY = (centerY - childSize.height / 2f).coerceIn(0f, maxY)
        val speed = kotlin.math.abs(insect.velocity.x).coerceAtLeast(2f)

        return listOf(-1f, 1f).mapIndexed { index, direction ->
            val centerX = insect.position.x + insect.size.width * (0.25f + index * 0.5f)
            Insect(
                id = firstId + index,
                type = childType,
                position = Offset(
                    x = (centerX - childSize.width / 2f).coerceIn(0f, maxX),
                    y = childY
                ),
                velocity = Offset(direction * speed, insect.velocity.y),
                size = childSize
            )
        }
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
        val minX = minOf(margin, (fieldSize.width - insectSize.width).coerceAtLeast(0f))
        val minY = minOf(margin, (fieldSize.height - insectSize.height).coerceAtLeast(0f))
        val maxX = (fieldSize.width - insectSize.width - margin).coerceAtLeast(minX)
        val maxY = (fieldSize.height - insectSize.height - margin).coerceAtLeast(minY)
        return Offset(
            x = minX + Random.nextFloat() * (maxX - minX),
            y = minY + Random.nextFloat() * (maxY - minY)
        )
    }

    fun spawnSingleInsect(
        fieldSize: Size,
        difficulty: Int,
        speedMultiplier: Float = 1.0f
    ): Insect {
        val type = randomType(difficulty)
        val baseSpeed = 2f + difficulty * 0.8f
        val insectSize = sizeFor(type, fieldSize)

        return Insect(
            id = System.currentTimeMillis(),
            type = type,
            position = randomPosition(fieldSize, insectSize),
            velocity = Offset(
                x = (Random.nextFloat() - 0.5f) * baseSpeed * speedMultiplier * 3f,
                y = (Random.nextFloat() - 0.5f) * baseSpeed * speedMultiplier * 3f
            ),
            size = insectSize
        )
    }

    fun spawnGoldenInsect(fieldSize: Size, difficulty: Int): Insect {
        val baseSpeed = 2f + difficulty * 0.8f
        val insectSize = 110f

        return Insect(
            id = System.currentTimeMillis() + 999,
            type = InsectType.GOLDEN,
            position = randomPosition(fieldSize, Size(insectSize, insectSize)),
            velocity = Offset(
                x = (Random.nextFloat() - 0.5f) * baseSpeed * 3f,
                y = (Random.nextFloat() - 0.5f) * baseSpeed * 3f
            ),
            size = Size(insectSize, insectSize)
        )
    }

    private fun randomType(difficulty: Int): InsectType {
        val centipedeChance = ((difficulty - 6) * 0.16f).coerceIn(0f, 0.64f)
        if (Random.nextFloat() < centipedeChance) return InsectType.CENTIPEDE

        val splitterChance = ((difficulty - 6) * 0.1f).coerceIn(0f, 0.36f)
        if (difficulty >= 7 && Random.nextFloat() < splitterChance) {
            return if (difficulty >= 9 && Random.nextFloat() < 0.35f) {
                InsectType.SPIDER
            } else {
                InsectType.ARMORED_BEETLE
            }
        }

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

    private fun sizeFor(type: InsectType, fieldSize: Size): Size =
        if (type == InsectType.CENTIPEDE) {
            Size(
                width = fieldSize.width.coerceAtLeast(1f).coerceAtMost(560f),
                height = fieldSize.height.coerceAtLeast(1f).coerceAtMost(180f)
            )
        } else {
            Size(width = 120f, height = 120f)
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