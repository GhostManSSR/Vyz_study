package BugBug.androidApp.ui.registration

import android.app.Application
import BugBug.androidApp.BugGameApp
import BugBug.androidApp.data.local.PlayerEntity
import BugBug.androidApp.data.repository.PlayerRepository
import BugBug.androidApp.domain.ZodiacCalculator
import BugBug.androidApp.model.Gender
import BugBug.androidApp.model.Player
import BugBug.androidApp.model.ZodiacSign
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.util.Calendar


class RegistrationViewModel(
    private val repository: PlayerRepository
) : ViewModel() {

    private val _state = MutableStateFlow(RegistrationUiState())
    val state: StateFlow<RegistrationUiState> = _state.asStateFlow()

    private val _savedPlayer = MutableStateFlow<Player?>(null)
    val savedPlayer: StateFlow<Player?> = _savedPlayer.asStateFlow()

    private val _savedPlayerId = MutableStateFlow(0L)
    val savedPlayerId: StateFlow<Long> = _savedPlayerId.asStateFlow()

    fun onNameChange(v: String)     = _state.update { it.copy(fullName = v) }
    fun onPasswordChange(v: String) = _state.update { it.copy(password = v) } // новое
    fun onGenderChange(v: Gender)   = _state.update { it.copy(gender = v) }
    fun onCourseChange(v: Int)      = _state.update { it.copy(course = v) }
    fun onDateChange(cal: Calendar) = _state.update { it.copy(birthDate = cal) }

    fun toggleLoginMode() = _state.update { it.copy(isLoginMode = !it.isLoginMode) } // новое

    fun onRegister(difficulty: Int, onSaved: () -> Unit) {

        val s = _state.value
        if (!s.isFormValid) {
            _state.update { it.copy(error = "Введите ФИО и пароль") }
            return
        }

        if (!s.isRegistrationFormValid) {
            _state.update { it.copy(error = "Проверьте правильность заполнения полей") }
            return
        }

        val player = Player(
            fullName = s.fullName,
            gender = s.gender,
            course = s.course,
            difficulty = difficulty,
            birthDate = s.birthDate,
            zodiac = ZodiacCalculator.calculate(s.birthDate),
            password = s.password
        )
        _savedPlayer.value = player

        viewModelScope.launch {
            val existing = repository.getPlayerByName(s.fullName)
            if (existing != null) {
                _state.update {
                    it.copy(error = "Игрок с таким именем уже существует. Используйте вход.")
                }
                return@launch
            }

            val id = repository.savePlayer(player)
            _savedPlayerId.value = id
            onSaved()
        }
    }

    fun onLogin(onLoggedIn: (Long) -> Unit) {
        val s = _state.value
        if (!s.isFormValid) {
            _state.update { it.copy(error = "Введите ФИО и пароль") }
            return
        }

        viewModelScope.launch {
            val playerEntity = repository.getPlayerByCredentials(s.fullName, s.password)
            if (playerEntity == null) {
                _state.update { it.copy(error = "Неверное имя или пароль") }
                return@launch
            }

            _savedPlayerId.value = playerEntity.id
            loadExistingPlayer(playerEntity)
            onLoggedIn(playerEntity.id)
        }
    }

    fun onErrorShown() = _state.update { it.copy(error = null) }

    fun loadExistingPlayer(entity: PlayerEntity) {
        _savedPlayerId.value = entity.id
        _savedPlayer.value = Player(
            fullName = entity.fullName,
            gender = entity.gender,
            course = entity.course,
            difficulty = entity.difficulty,
            birthDate = entity.birthDate,
            zodiac = ZodiacSign.entries.firstOrNull { it.title == entity.zodiacName }
                ?: ZodiacSign.ARIES,
            password = entity.password
        )
        _state.update {
            it.copy(
                fullName = entity.fullName,
                gender = entity.gender,
                course = entity.course,
                birthDate = entity.birthDate
            )
        }
    }
}