package BugBug.androidApp.data.repository

import BugBug.androidApp.data.remote.RetrofitClient
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class GoldRepository {

    suspend fun getGoldRate(): Double {
        return try {
            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

            val cal2 = Calendar.getInstance()
            val cal1 = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }

            val response = RetrofitClient.goldApi.getGoldRate(
                date1 = fmt.format(cal1.time),
                date2 = fmt.format(cal2.time)
            )

            // Золото в CBR имеет Code = 1
            // Берём последнюю запись (самая свежая)
            val goldRecord = response.records
                .lastOrNull { it.code == "1" }

            goldRecord?.buy
                ?.replace(",", ".")
                ?.toDoubleOrNull() ?: 0.0
        } catch (e: Exception) {
            e.printStackTrace()
            0.0
        }
    }
}