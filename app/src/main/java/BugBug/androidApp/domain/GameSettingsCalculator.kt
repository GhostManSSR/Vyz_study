package BugBug.androidApp.domain

import BugBug.androidApp.model.GameSettings
import kotlin.math.roundToInt


enum class SettingField {
    SPEED,
    MAX_COCKROACHES,
    BONUS_INTERVAL,
    ROUND_DURATION
}


object GameSettingsCalculator {

    const val MIN_DIFFICULTY = 0
    const val MAX_DIFFICULTY = 10

    private const val SPEED_MIN = 0.5f
    private const val SPEED_STEP = 0.25f
    private const val SPEED_LEVELS = 6          // 0.5 .. 2.0 -> 7 позиций

    private const val COCKROACH_MIN = 5
    private const val COCKROACH_MAX = 30

    private const val BONUS_MIN = 10
    private const val BONUS_MAX = 60
    private const val BONUS_STEP = 5

    private const val ROUND_MIN = 30
    private const val ROUND_MAX = 180
    private const val ROUND_STEP = 10

    /** 0 -> 0.5x, 10 -> 2.0x */
    fun recommendedSpeed(difficulty: Int): Float {
        val d = difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
        val level = (d * SPEED_LEVELS / MAX_DIFFICULTY.toFloat()).roundToInt()
        return SPEED_MIN + level * SPEED_STEP
    }

    /** 0 -> 5, 10 -> 30 */
    fun recommendedMaxCockroaches(difficulty: Int): Int {
        val d = difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
        val span = COCKROACH_MAX - COCKROACH_MIN
        return COCKROACH_MIN + (d * span / MAX_DIFFICULTY.toFloat()).roundToInt()
    }

    /** Чем выше сложность, тем чаще бонусы: 0 -> 60 сек, 10 -> 10 сек */
    fun recommendedBonusIntervalSec(difficulty: Int): Int {
        val d = difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
        val span = BONUS_MAX - BONUS_MIN
        val raw = BONUS_MAX - d * span / MAX_DIFFICULTY.toFloat()
        return snapToStep(raw, BONUS_MIN, BONUS_MAX, BONUS_STEP)
    }

    /** 0 -> 30 сек, 10 -> 180 сек */
    fun recommendedRoundDurationSec(difficulty: Int): Int {
        val d = difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
        val raw = ROUND_MIN + d * (ROUND_MAX - ROUND_MIN) / MAX_DIFFICULTY.toFloat()
        return snapToStep(raw, ROUND_MIN, ROUND_MAX, ROUND_STEP)
    }

    /**
     * Пересчитывает настройки под новую сложность.
     * Поля из [manualFields] остаются такими, какими их выставил игрок.
     */
    fun applyDifficulty(
        base: GameSettings,
        manualFields: Set<SettingField>
    ): GameSettings {
        val d = base.difficulty.coerceIn(MIN_DIFFICULTY, MAX_DIFFICULTY)
        return base.copy(
            difficulty = d,
            speed = if (SettingField.SPEED in manualFields) base.speed
            else recommendedSpeed(d),
            maxCockroaches = if (SettingField.MAX_COCKROACHES in manualFields) base.maxCockroaches
            else recommendedMaxCockroaches(d),
            bonusIntervalSec = if (SettingField.BONUS_INTERVAL in manualFields) base.bonusIntervalSec
            else recommendedBonusIntervalSec(d),
            roundDurationSec = if (SettingField.ROUND_DURATION in manualFields) base.roundDurationSec
            else recommendedRoundDurationSec(d)
        )
    }

    /** Поля, значения которых совпадают с рекомендациями для текущей сложности. */
    fun autoFieldsFor(settings: GameSettings): Set<SettingField> = buildSet {
        if (settings.speed == recommendedSpeed(settings.difficulty)) add(SettingField.SPEED)
        if (settings.maxCockroaches == recommendedMaxCockroaches(settings.difficulty)) add(SettingField.MAX_COCKROACHES)
        if (settings.bonusIntervalSec == recommendedBonusIntervalSec(settings.difficulty)) add(SettingField.BONUS_INTERVAL)
        if (settings.roundDurationSec == recommendedRoundDurationSec(settings.difficulty)) add(SettingField.ROUND_DURATION)
    }

    private fun snapToStep(value: Float, min: Int, max: Int, step: Int): Int {
        val snapped = (value / step).roundToInt() * step
        return snapped.coerceIn(min, max)
    }
}