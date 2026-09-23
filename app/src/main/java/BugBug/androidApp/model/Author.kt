package BugBug.androidApp.model

import androidx.annotation.DrawableRes

data class Author(
    val name: String,
    val group: String,
    val role: String,
    @DrawableRes val photoResId: Int = 0
)