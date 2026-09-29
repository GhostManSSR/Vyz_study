package BugBug.androidApp.ui.registration

import BugBug.androidApp.domain.ZodiacCalculator
import BugBug.androidApp.model.Gender
import BugBug.androidApp.model.ZodiacSign
import java.util.Calendar

data class RegistrationUiState(
    val fullName: String = "",
    val gender: Gender = Gender.MALE,
    val course: Int = 1,
    val birthDate: Calendar = Calendar.getInstance(),
    val error: String? = null
) {
    val isFormValid: Boolean get() = fullName.isNotBlank()
    val zodiac: ZodiacSign get() = ZodiacCalculator.calculate(birthDate)
}