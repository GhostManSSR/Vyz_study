package BugBug.androidApp.data.model

import androidx.annotation.DrawableRes
import BugBug.androidApp.R

enum class ZodiacSign(
    val title: String,
    @DrawableRes val iconRes: Int
) {
    ARIES      ("Овен",      R.drawable.aries_symbol),
    TAURUS     ("Телец",     R.drawable.taurus_zodiac_symbol_of_bull_head_front),
    GEMINI     ("Близнецы",  R.drawable.gemini_zodiac_symbol_of_two_twins_faces),
    CANCER     ("Рак",       R.drawable.crab_cancer_symbol),
    LEO        ("Лев",       R.drawable.leo_astrological_sign),
    VIRGO      ("Дева",      R.drawable.virgo_female_silhouette),
    LIBRA      ("Весы",      R.drawable.libra_scale_balance_symbol),
    SCORPIO    ("Скорпион",  R.drawable.scorpion_shape_of_zodiac_sign),
    SAGITTARIUS("Стрелец",   R.drawable.sagittarius_arch_and_arrow_symbol),
    CAPRICORN  ("Козерог",   R.drawable.capricorn_symbol),
    AQUARIUS   ("Водолей",   R.drawable.aquarius_symbol),
    PISCES     ("Рыбы",      R.drawable.pisces_astrological_sign_symbol)
}