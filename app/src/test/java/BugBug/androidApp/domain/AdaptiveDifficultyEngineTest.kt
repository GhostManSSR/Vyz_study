package BugBug.androidApp.domain

import BugBug.androidApp.data.local.ScoreEntity
import BugBug.androidApp.model.GameSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class AdaptiveDifficultyEngineTest {

    private fun game(
        hits: Int,
        misses: Int,
        score: Int = 100,
        difficulty: Int = 5
    ): ScoreEntity = ScoreEntity(
        playerId = 1L,
        score = score,
        hits = hits,
        misses = misses,
        difficulty = difficulty,
        roundDurationSec = 60
    )

    private val baseSettings = GameSettings(
        speed = 1.0f,
        maxCockroaches = 10,
        bonusIntervalSec = 15,
        roundDurationSec = 60,
        difficulty = 5,
        adaptiveModeEnabled = true
    )

    @Test
    fun highAccuracyWithEnoughHitsIncreasesDifficulty() {
        val games = listOf(
            game(hits = 15, misses = 3),
            game(hits = 17, misses = 3),
            game(hits = 18, misses = 2)
        )
        val result = AdaptiveDifficultyEngine.analyze(games, baseSettings)

        assertTrue("Ожидалось Increase, получили: $result", result is AdaptiveDecision.Increase)

        val inc = result as AdaptiveDecision.Increase
        assertEquals("Сложность должна стать 6", 6, inc.newSettings.difficulty)
        assertEquals("Скорость должна стать 1.1", 1.1f, inc.newSettings.speed, 0.01f)
        assertEquals("Жуков должно стать 12", 12, inc.newSettings.maxCockroaches)
        assertEquals("Бонусы должны стать 18", 18, inc.newSettings.bonusIntervalSec)
    }

    @Test
    fun lowAccuracyWithEnoughTapsDecreasesDifficulty() {
        val games = listOf(
            game(hits = 3, misses = 7),
            game(hits = 2, misses = 8),
            game(hits = 4, misses = 6)
        )
        val result = AdaptiveDifficultyEngine.analyze(games, baseSettings)

        assertTrue("Ожидалось Decrease, получили: $result", result is AdaptiveDecision.Decrease)

        val dec = result as AdaptiveDecision.Decrease
        assertEquals("Сложность должна стать 4", 4, dec.newSettings.difficulty)
        assertEquals("Скорость должна стать 0.9", 0.9f, dec.newSettings.speed, 0.01f)
    }

    @Test
    fun singleHitsPerGameDoNotIncreaseDifficulty() {
        val games = listOf(
            game(hits = 1, misses = 0),
            game(hits = 1, misses = 0),
            game(hits = 2, misses = 0)
        )
        val result = AdaptiveDifficultyEngine.analyze(games, baseSettings)

        assertTrue("Одиночные клики не должны повышать сложность. Получили: $result",
            result is AdaptiveDecision.NoChange)
    }

    @Test
    fun normalAccuracyDoesNotChangeSettings() {
        val games = listOf(
            game(hits = 6, misses = 4),
            game(hits = 5, misses = 5),
            game(hits = 7, misses = 3)
        )
        val result = AdaptiveDifficultyEngine.analyze(games, baseSettings)

        assertTrue("Точность в норме не должна менять настройки. Получили: $result",
            result is AdaptiveDecision.NoChange)
    }

    @Test
    fun roundWindowIncreasesAllGameplayParameters() {
        val result = AdaptiveDifficultyEngine.adjustDuringRound(
            current = baseSettings,
            hits = 8,
            misses = 2
        )

        assertTrue(result is AdaptiveRoundDecision.Changed)
        val changed = result as AdaptiveRoundDecision.Changed
        assertEquals(6, changed.newSettings.difficulty)
        assertEquals(1.1f, changed.newSettings.speed, 0.01f)
        assertEquals(12, changed.newSettings.maxCockroaches)
        assertEquals(18, changed.newSettings.bonusIntervalSec)
        assertEquals(80f, changed.accuracy, 0.01f)
    }

    @Test
    fun roundWindowWithLowAccuracyDecreasesSettings() {
        val result = AdaptiveDifficultyEngine.adjustDuringRound(
            current = baseSettings,
            hits = 4,
            misses = 6
        )

        assertTrue(result is AdaptiveRoundDecision.Changed)
        val changed = result as AdaptiveRoundDecision.Changed
        assertEquals(4, changed.newSettings.difficulty)
        assertEquals(0.9f, changed.newSettings.speed, 0.01f)
        assertEquals(8, changed.newSettings.maxCockroaches)
        assertEquals(12, changed.newSettings.bonusIntervalSec)
    }

    @Test
    fun roundWindowRequiresTenAttemptsAndEnoughHitsToIncrease() {
        val tooFewAttempts = AdaptiveDifficultyEngine.adjustDuringRound(
            current = baseSettings,
            hits = 7,
            misses = 2
        )
        val notEnoughHits = AdaptiveDifficultyEngine.adjustDuringRound(
            current = baseSettings,
            hits = 7,
            misses = 3
        )

        assertTrue(tooFewAttempts is AdaptiveRoundDecision.NoChange)
        assertTrue(notEnoughHits is AdaptiveRoundDecision.NoChange)
    }

    @Test
    fun roundWindowRespectsParameterLimits() {
        val increaseAtMaximum = AdaptiveDifficultyEngine.adjustDuringRound(
            current = baseSettings.copy(
                difficulty = AdaptiveDifficultyEngine.DIFFICULTY_MAX,
                speed = AdaptiveDifficultyEngine.SPEED_MAX,
                maxCockroaches = AdaptiveDifficultyEngine.COCKROACHES_MAX,
                bonusIntervalSec = AdaptiveDifficultyEngine.BONUS_INTERVAL_MAX
            ),
            hits = 10,
            misses = 0
        )
        val decreaseAtMinimum = AdaptiveDifficultyEngine.adjustDuringRound(
            current = baseSettings.copy(
                difficulty = AdaptiveDifficultyEngine.DIFFICULTY_MIN,
                speed = AdaptiveDifficultyEngine.SPEED_MIN,
                maxCockroaches = AdaptiveDifficultyEngine.COCKROACHES_MIN,
                bonusIntervalSec = AdaptiveDifficultyEngine.BONUS_INTERVAL_MIN
            ),
            hits = 0,
            misses = 10
        )

        assertTrue(increaseAtMaximum is AdaptiveRoundDecision.NoChange)
        assertTrue(decreaseAtMinimum is AdaptiveRoundDecision.NoChange)
    }
}