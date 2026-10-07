package BugBug.androidApp.ui.registration

import BugBug.androidApp.domain.ZodiacCalculator
import BugBug.androidApp.model.Gender
import BugBug.androidApp.model.ZodiacSign
import java.util.Calendar

data class RegistrationUiState(
    val fullName: String = "",
    val gender: Gender = Gender.MALE,
    val course: Int = 1,
    val isLoginMode: Boolean = false,
    val password: String = "",
    val birthDate: Calendar = Calendar.getInstance(),
    val error: String? = null
) {
    val isFormValid: Boolean get() = fullName.isNotBlank()

    val isLoginFormValid: Boolean get() = fullName.isNotBlank() && password.isNotBlank()

    val isRegistrationFormValid: Boolean get() =
        fullName.isNotBlank() && password.isNotBlank() && passwordError == null

    val passwordError: String? get() = when {
        password.isEmpty() -> null
        password.length < 6 -> "Минимум 6 символов"
        !password.any { it.isDigit() } -> "Должна быть хотя бы одна цифра"
        !password.any { it.isLetter() } -> "Должна быть хотя бы одна буква"
        else -> null
    }

    val zodiac: ZodiacSign get() = ZodiacCalculator.calculate(birthDate)
}