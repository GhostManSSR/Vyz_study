package BugBug.androidApp.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import BugBug.androidApp.model.Gender
import java.util.Calendar

@Entity(tableName = "players")
data class PlayerEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val fullName: String,
    val gender: Gender,
    val course: Int,
    val difficulty: Int,
    val birthDate: Calendar,
    val zodiacName: String,
    val password: String = "",
    val createdAt: Long = System.currentTimeMillis()
)