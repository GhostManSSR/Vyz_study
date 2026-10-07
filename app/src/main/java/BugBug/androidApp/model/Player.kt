package BugBug.androidApp.model

import java.util.Calendar

data class Player(
    val fullName: String,
    val gender: Gender,
    val course: Int,
    val difficulty: Int,
    val birthDate: Calendar,
    val password: String = "",
    val zodiac: ZodiacSign
)