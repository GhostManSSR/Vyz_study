package BugBug.androidApp.data.repository

import BugBug.androidApp.model.GameSettings
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository {

    private val _settings = MutableStateFlow(GameSettings())
    val settings: StateFlow<GameSettings> = _settings.asStateFlow()

    fun update(newSettings: GameSettings) {
        _settings.value = newSettings
    }

    fun reset() {
        _settings.value = GameSettings()
    }
}