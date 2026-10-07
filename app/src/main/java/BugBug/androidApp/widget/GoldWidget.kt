package BugBug.androidApp.widget

import android.annotation.SuppressLint
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.cornerRadius
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.layout.*
import androidx.glance.text.*
import androidx.glance.unit.ColorProvider
import BugBug.androidApp.R
import BugBug.androidApp.data.repository.GoldRate
import BugBug.androidApp.data.repository.GoldRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

class GoldWidget : GlanceAppWidget() {

    private val repository = GoldRepository()

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rate = runCatching { repository.getGoldRate() }
            .getOrElse { GoldRate(value = 0.0, previousValue = null, updatedAt = Date()) }

        provideContent {
            GoldContent(rate)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun GoldContent(rate: GoldRate) {
        android.util.Log.d("GoldWidget", "render value=${rate.value} hasPrice=${rate.value > 0.0}")
        val hasPrice = rate.value > 0.0

        val priceText = if (hasPrice) {
            String.format(Locale.getDefault(), "%,.2f", rate.value)
                .replace('\u00A0', ' ') + " ₽"
        } else {
            "—"
        }

        val updatedText = SimpleDateFormat("HH:mm · dd.MM", Locale.getDefault())
            .format(rate.updatedAt)

        val change = rate.change
        val percent = rate.changePercent
        val hasDelta = hasPrice && change != null && percent != null
        val isUp = (change ?: 0.0) >= 0.0

        val chipColor = if (isUp) ColorProvider(R.color.teal_700) else ColorProvider(R.color.purple_700)

        val chipText = if (hasDelta) {
            val arrow = if (isUp) "▲" else "▼"
            "$arrow ${String.format(Locale.getDefault(), "%.2f", abs(percent ?: 0.0))}%"
        } else {
            "—"
        }

        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(R.color.widget_background))
                .cornerRadius(24.dp)
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = GlanceModifier.fillMaxSize(),
                verticalAlignment = Alignment.Vertical.CenterVertically,
                horizontalAlignment = Alignment.Horizontal.Start
            ) {

                Row(
                    modifier = GlanceModifier.fillMaxWidth(),
                    verticalAlignment = Alignment.Vertical.CenterVertically
                ) {
                    Box(
                        modifier = GlanceModifier
                            .size(8.dp)
                            .background(ColorProvider(R.color.purple_500))
                            .cornerRadius(4.dp)
                    ) {}

                    Spacer(GlanceModifier.width(6.dp))

                    Text(
                        text = "ЗОЛОТО · ЦБ РФ",
                        style = TextStyle(
                            color = ColorProvider(R.color.widget_title),
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )

                    Spacer(GlanceModifier.defaultWeight())

                    Text(
                        text = chipText,
                        style = TextStyle(
                            color = chipColor,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    )
                }

                Spacer(GlanceModifier.height(10.dp))

                Text(
                    text = priceText,
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_price),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold
                    )
                )

                Spacer(GlanceModifier.height(2.dp))

                Text(
                    text = if (hasPrice) "за 1 грамм" else "нет данных",
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_time),
                        fontSize = 10.sp
                    )
                )

                Spacer(GlanceModifier.defaultWeight())

                Text(
                    text = "обновлено $updatedText",
                    style = TextStyle(
                        color = ColorProvider(R.color.widget_time),
                        fontSize = 10.sp
                    )
                )
            }
        }
    }
}