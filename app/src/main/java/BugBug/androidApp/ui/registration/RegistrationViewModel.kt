package BugBug.androidApp.ui.registration
import BugBug.androidApp.domain.ZodiacCalculator
import BugBug.androidApp.model.Gender
import BugBug.androidApp.model.Player
import BugBug.androidApp.model.ZodiacSign
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.util.Calendar

data class RegistrationUiState(
    val fullName: String = "",
    val gender: Gender = Gender.MALE,
    val course: Int = 1,
    val difficulty: Int = 5,
    val birthDate: Calendar = Calendar.getInstance(),
    val error: String? = null
) {
    val isFormValid: Boolean get() = fullName.isNotBlank()
    val zodiac: ZodiacSign get() = ZodiacCalculator.calculate(birthDate)
}

class RegistrationViewModel : ViewModel() {

    private val _state = MutableStateFlow(RegistrationUiState())
    val state: StateFlow<RegistrationUiState> = _state.asStateFlow()

    private val _savedPlayer = MutableStateFlow<Player?>(null)
    val savedPlayer: StateFlow<Player?> = _savedPlayer.asStateFlow()

    fun onNameChange(v: String)     = _state.update { it.copy(fullName = v) }
    fun onGenderChange(v: Gender)   = _state.update { it.copy(gender = v) }
    fun onCourseChange(v: Int)      = _state.update { it.copy(course = v) }
    fun onDifficultyChange(v: Int)  = _state.update { it.copy(difficulty = v) }
    fun onDateChange(cal: Calendar) = _state.update { it.copy(birthDate = cal) }

    fun onRegister(): Boolean {
        val s = _state.value
        if (!s.isFormValid) {
            _state.update { it.copy(error = "Введите ФИО") }
            return false
        }
        _savedPlayer.value = Player(
            fullName = s.fullName,
            gender = s.gender,
            course = s.course,
            difficulty = s.difficulty,
            birthDate = s.birthDate,
            zodiac = ZodiacCalculator.calculate(s.birthDate)
        )
        return true
    }

    fun onErrorShown() = _state.update { it.copy(error = null) }
}