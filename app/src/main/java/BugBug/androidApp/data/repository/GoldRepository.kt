package BugBug.androidApp.data.repository

import BugBug.androidApp.data.remote.RetrofitClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class GoldRate(
    val value: Double,
    val previousValue: Double? = null,
    val updatedAt: Date = Date()
) {
    val change: Double? get() = previousValue?.takeIf { it > 0.0 }?.let { value - it }
    val changePercent: Double? get() = previousValue?.takeIf { it > 0.0 }?.let { (value - it) / it * 100.0 }
}

class GoldRepository {

    suspend fun getGoldRate(): GoldRate = withContext(Dispatchers.IO) {
        try {
            val fmt = SimpleDateFormat("dd/MM/yyyy", Locale.US)
            val dateTo = Calendar.getInstance()
            val dateFrom = Calendar.getInstance().apply { add(Calendar.DAY_OF_MONTH, -7) }

            val response = RetrofitClient.goldApi.getGoldRate(
                date1 = fmt.format(dateFrom.time),
                date2 = fmt.format(dateTo.time)
            )

            android.util.Log.d("GoldWidget", "records total = ${response.records.size}")
            response.records.forEach {
                android.util.Log.d("GoldWidget", "code=${it.code} buy='${it.buy}'")
            }

            val goldRecords = response.records.filter { it.code == "1" }
            android.util.Log.d("GoldWidget", "gold records = ${goldRecords.size}")

            GoldRate(
                value = goldRecords.lastOrNull()?.buy.toDoubleOrZero(),
                previousValue = goldRecords.dropLast(1).lastOrNull()?.buy.toDoubleOrZero().takeIf { it > 0.0 },
                updatedAt = Date()
            )
        } catch (e: Exception) {
            android.util.Log.e("GoldWidget", "getGoldRate failed", e)
            GoldRate(value = 0.0)
        }
    }

    private fun String?.toDoubleOrZero(): Double =
        this?.replace(",", ".")?.replace(" ", "")?.toDoubleOrNull() ?: 0.0
}