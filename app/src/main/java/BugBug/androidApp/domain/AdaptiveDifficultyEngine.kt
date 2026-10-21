package BugBug.androidApp.domain

import BugBug.androidApp.data.local.ScoreEntity
import BugBug.androidApp.model.GameSettings

object AdaptiveDifficultyEngine {

    const val MIN_GAMES_FOR_ANALYSIS = 2
    const val MAX_GAMES_TO_ANALYZE = 3
    const val MIN_TAPS_PER_GAME = 5
    const val MIN_TOTAL_TAPS_FOR_DECREASE = 15
    const val ROUND_WINDOW_SIZE = 10
    const val ROUND_HIGH_ACCURACY_THRESHOLD = 70f
    const val ROUND_MIN_HITS_FOR_INCREASE = 8
    const val ROUND_LOW_ACCURACY_THRESHOLD = 40f

    const val HIGH_ACCURACY_THRESHOLD = 70f
    const val MIN_HITS_FOR_INCREASE = 8

    const val LOW_ACCURACY_THRESHOLD = 40f

    const val SPEED_MIN = 0.5f
    const val SPEED_MAX = 2.0f
    const val SPEED_STEP = 0.1f
    const val COCKROACHES_MIN = 5
    const val COCKROACHES_MAX = 30
    const val COCKROACHES_STEP = 2
    const val BONUS_INTERVAL_MIN = 10
    const val BONUS_INTERVAL_MAX = 60
    const val BONUS_INTERVAL_STEP = 3
    const val DIFFICULTY_MIN = 0
    const val DIFFICULTY_MAX = 10

    fun adjustDuringRound(
        current: GameSettings,
        hits: Int,
        misses: Int
    ): AdaptiveRoundDecision {
        val attempts = hits + misses
        if (attempts < ROUND_WINDOW_SIZE) {
            return AdaptiveRoundDecision.NoChange(
                "Для оценки нужно $ROUND_WINDOW_SIZE попаданий или промахов"
            )
        }

        val accuracy = if (attempts == 0) 0f else hits.toFloat() / attempts * 100f
        val shouldIncrease =
            accuracy >= ROUND_HIGH_ACCURACY_THRESHOLD &&
                    hits >= ROUND_MIN_HITS_FOR_INCREASE
        val shouldDecrease = accuracy <= ROUND_LOW_ACCURACY_THRESHOLD

        val updatedSettings = when {
            shouldIncrease -> applyIncrease(current)
            shouldDecrease -> applyDecrease(current)
            else -> current
        }

        if (updatedSettings == current) {
            return AdaptiveRoundDecision.NoChange(
                when {
                    shouldIncrease || shouldDecrease ->
                        "Достигнута граница адаптивных настроек"
                    else -> "Точность ${"%.0f".format(accuracy)}% — без изменений"
                }
            )
        }

        return AdaptiveRoundDecision.Changed(
            newSettings = updatedSettings,
            accuracy = accuracy,
            hits = hits,
            misses = misses,
            increased = shouldIncrease
        )
    }

    fun analyze(
        recentGames: List<ScoreEntity>,
        current: GameSettings
    ): AdaptiveDecision {

        val played = recentGames
            .filter { it.hits + it.misses >= MIN_TAPS_PER_GAME }
            .take(MAX_GAMES_TO_ANALYZE)   // самые свежие (DAO отдаёт DESC)

        if (played.size < MIN_GAMES_FOR_ANALYSIS) {
            return AdaptiveDecision.NoChange(
                reason = "Мало данных: сыграйте $MIN_GAMES_FOR_ANALYSIS раза " +
                        "с $MIN_TAPS_PER_GAME+ тапами"
            )
        }

        val avgAccuracy = played.map { it.accuracyPercent }.average().toFloat()
        val avgHits = played.map { it.hits }.average().toFloat()
        val avgScore = played.map { it.score }.average().toFloat()

        val totalHits = played.sumOf { it.hits }
        val totalTaps = played.sumOf { it.hits + it.misses }

        val bestScore = played.maxOf { it.score }
        val scoreRatio = if (bestScore > 0) avgScore / bestScore else 0f

        val shouldIncrease =
            avgAccuracy >= HIGH_ACCURACY_THRESHOLD &&
                    avgHits >= MIN_HITS_FOR_INCREASE

        val shouldDecrease =
            avgAccuracy <= LOW_ACCURACY_THRESHOLD &&
                    totalTaps >= MIN_TOTAL_TAPS_FOR_DECREASE

        return when {
            shouldIncrease -> AdaptiveDecision.Increase(
                newSettings = applyIncrease(current),
                avgAccuracy = avgAccuracy,
                avgHits = avgHits,
                avgScore = avgScore,
                scoreRatio = scoreRatio
            )
            shouldDecrease -> AdaptiveDecision.Decrease(
                newSettings = applyDecrease(current),
                avgAccuracy = avgAccuracy,
                avgHits = avgHits,
                avgScore = avgScore,
                scoreRatio = scoreRatio
            )
            else -> AdaptiveDecision.NoChange(
                reason = buildNoChangeReason(avgAccuracy, avgHits)
            )
        }
    }

    private fun buildNoChangeReason(avgAccuracy: Float, avgHits: Float): String {
        val acc = "%.0f%%".format(avgAccuracy)
        val hits = "%.1f".format(avgHits)

        return when {
            avgAccuracy >= HIGH_ACCURACY_THRESHOLD ->
                "Точность $acc — отлично, но попаданий $hits < $MIN_HITS_FOR_INCREASE"
            avgAccuracy > LOW_ACCURACY_THRESHOLD ->
                "Точность $acc — в норме (нужно ≥$HIGH_ACCURACY_THRESHOLD% для повышения)"
            else ->
                "Точность $acc — низкая, но было мало попыток"
        }
    }

    private fun applyIncrease(current: GameSettings): GameSettings = current.copy(
        difficulty = (current.difficulty + 1).coerceAtMost(DIFFICULTY_MAX),
        speed = (current.speed + SPEED_STEP).coerceAtMost(SPEED_MAX),
        maxCockroaches = (current.maxCockroaches + COCKROACHES_STEP).coerceAtMost(COCKROACHES_MAX),
        bonusIntervalSec = (current.bonusIntervalSec + BONUS_INTERVAL_STEP).coerceAtMost(BONUS_INTERVAL_MAX)
    )

    private fun applyDecrease(current: GameSettings): GameSettings = current.copy(
        difficulty = (current.difficulty - 1).coerceAtLeast(DIFFICULTY_MIN),
        speed = (current.speed - SPEED_STEP).coerceAtLeast(SPEED_MIN),
        maxCockroaches = (current.maxCockroaches - COCKROACHES_STEP).coerceAtLeast(COCKROACHES_MIN),
        bonusIntervalSec = (current.bonusIntervalSec - BONUS_INTERVAL_STEP).coerceAtLeast(BONUS_INTERVAL_MIN)
    )
}

sealed class AdaptiveDecision {
    data class Increase(
        val newSettings: GameSettings,
        val avgAccuracy: Float,
        val avgHits: Float,
        val avgScore: Float,
        val scoreRatio: Float
    ) : AdaptiveDecision()

    data class Decrease(
        val newSettings: GameSettings,
        val avgAccuracy: Float,
        val avgHits: Float,
        val avgScore: Float,
        val scoreRatio: Float
    ) : AdaptiveDecision()

    data class NoChange(val reason: String) : AdaptiveDecision()
}

sealed class AdaptiveRoundDecision {
    data class Changed(
        val newSettings: GameSettings,
        val accuracy: Float,
        val hits: Int,
        val misses: Int,
        val increased: Boolean
    ) : AdaptiveRoundDecision()

    data class NoChange(val reason: String) : AdaptiveRoundDecision()
}