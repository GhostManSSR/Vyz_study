package BugBug.androidApp.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import BugBug.androidApp.data.repository.SettingsRepository
import BugBug.androidApp.domain.GameSettingsCalculator
import BugBug.androidApp.domain.SettingField
import BugBug.androidApp.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn

class GameSettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val settings: StateFlow<GameSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.Eagerly, GameSettings())

    private val _manualFields = MutableStateFlow<Set<SettingField>>(emptySet())
    val manualFields: StateFlow<Set<SettingField>> = _manualFields.asStateFlow()

    fun updateSettings(newSettings: GameSettings) {
        val current = settings.value
        if (current.adaptiveModeEnabled) return
        if (newSettings == current) return

        if (newSettings.difficulty != current.difficulty) {
            settingsRepository.update(
                GameSettingsCalculator.applyDifficulty(
                    base = newSettings,
                    manualFields = _manualFields.value
                )
            )
            return
        }

        _manualFields.value = resolveManualFields(current, newSettings)
        settingsRepository.update(newSettings)
    }

    fun setAdaptiveMode(enabled: Boolean) {
        val current = settings.value

        if (enabled) {
            _manualFields.value = emptySet()
            val base = GameSettingsCalculator.applyDifficulty(
                base = current.copy(adaptiveModeEnabled = true, difficulty = 5),
                manualFields = emptySet()
            )
            settingsRepository.update(base)
        } else {
            settingsRepository.update(current.copy(adaptiveModeEnabled = false))
        }
    }

    fun applyAdaptiveResult(newSettings: GameSettings) {
        if (!settings.value.adaptiveModeEnabled) return
        _manualFields.value = emptySet()
        settingsRepository.update(newSettings.copy(adaptiveModeEnabled = true))
    }

    fun resetSettings() {
        _manualFields.value = emptySet()
        settingsRepository.reset()
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