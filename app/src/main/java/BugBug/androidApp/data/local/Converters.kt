package BugBug.androidApp.data.local

import androidx.room.TypeConverter
import BugBug.androidApp.model.Gender
import java.util.Calendar

class Converters {

    @TypeConverter
    fun calendarToLong(value: Calendar?): Long? = value?.timeInMillis

    @TypeConverter
    fun longToCalendar(value: Long?): Calendar? =
        value?.let {
            Calendar.getInstance().apply { timeInMillis = it }
        }

    @TypeConverter
    fun genderToString(value: Gender?): String? = value?.name

    @TypeConverter
    fun stringToGender(value: String?): Gender? =
        value?.let { Gender.valueOf(it) }
}