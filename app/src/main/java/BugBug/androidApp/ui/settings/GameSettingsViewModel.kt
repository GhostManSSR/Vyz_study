// ui/settings/GameSettingsViewModel.kt
package BugBug.androidApp.ui.settings

import androidx.lifecycle.ViewModel
import BugBug.androidApp.domain.GameSettingsCalculator
import BugBug.androidApp.domain.SettingField
import BugBug.androidApp.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class GameSettingsViewModel : ViewModel() {

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    /** Поля, которые игрок выставил сам и которые больше не зависят от сложности. */
    private val _manualFields = MutableStateFlow<Set<SettingField>>(emptySet())
    val manualFields: StateFlow<Set<SettingField>> = _manualFields.asStateFlow()

    /**
     * Единая точка входа для UI: сюда приходит уже готовый GameSettings.
     * Метод сам понимает, что именно изменилось.
     */
    fun updateSettings(newSettings: GameSettings) {
        val current = _settings.value
        if (newSettings == current) return

        // 1. Изменили сложность -> подтягиваем связанные настройки,
        //    кроме тех, что игрок выставил вручную.
        if (newSettings.difficulty != current.difficulty) {
            _settings.value = GameSettingsCalculator.applyDifficulty(
                base = newSettings,
                manualFields = _manualFields.value
            )
            return
        }

        // 2. Изменили что-то другое -> помечаем поле как ручное.
        //    Если игрок вручную вернул значение к рекомендованному —
        //    снова считаем поле автоматическим.
        _manualFields.value = resolveManualFields(current, newSettings)
        _settings.value = newSettings
    }

    fun resetSettings() {
        _manualFields.value = emptySet()
        _settings.value = GameSettings()
    }

    /** Позволяет вернуть конкретное поле в «авто»-режим (например, по кнопке в UI). */
    fun unlinkField(field: SettingField) {
        _manualFields.value = _manualFields.value - field
        _settings.value = GameSettingsCalculator.applyDifficulty(
            base = _settings.value,
            manualFields = _manualFields.value
        )
    }

    private fun resolveManualFields(
        current: GameSettings,
        updated: GameSettings
    ): Set<SettingField> {
        val d = updated.difficulty
        var result = _manualFields.value

        fun handle(field: SettingField, changed: Boolean, isRecommended: Boolean) {
            if (!changed) return
            result = if (isRecommended) result - field else result + field
        }

        handle(
            SettingField.SPEED,
            updated.speed != current.speed,
            updated.speed == GameSettingsCalculator.recommendedSpeed(d)
        )
        handle(
            SettingField.MAX_COCKROACHES,
            updated.maxCockroaches != current.maxCockroaches,
            updated.maxCockroaches == GameSettingsCalculator.recommendedMaxCockroaches(d)
        )
        handle(
            SettingField.BONUS_INTERVAL,
            updated.bonusIntervalSec != current.bonusIntervalSec,
            updated.bonusIntervalSec == GameSettingsCalculator.recommendedBonusIntervalSec(d)
        )
        handle(
            SettingField.ROUND_DURATION,
            updated.roundDurationSec != current.roundDurationSec,
            updated.roundDurationSec == GameSettingsCalculator.recommendedRoundDurationSec(d)
        )

        return result
    }
}