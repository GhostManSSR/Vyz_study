package BugBug.androidApp.widget

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.provideContent
import androidx.glance.background
import androidx.glance.color.ColorProvider
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import BugBug.androidApp.data.repository.GoldRepository
import android.annotation.SuppressLint
import androidx.glance.unit.ColorProvider

class GoldWidget : GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val rate = GoldRepository().getGoldRate()
        provideContent {
            GoldContent(rate)
        }
    }

    @SuppressLint("RestrictedApi")
    @Composable
    private fun GoldContent(rate: Double) {
        Box(
            modifier = GlanceModifier
                .fillMaxSize()
                .background(ColorProvider(Color(0xFFFFD700))),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (rate > 0) "%.2f ₽".format(rate) else "---",
                style = TextStyle(
                    color = ColorProvider(Color.Black),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}