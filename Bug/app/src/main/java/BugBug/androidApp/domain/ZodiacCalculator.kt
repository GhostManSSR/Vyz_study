package BugBug.androidApp.domain

import BugBug.androidApp.model.ZodiacSign
import java.util.Calendar

object ZodiacCalculator {
    fun calculate(date: Calendar): ZodiacSign {
        val d = date.get(Calendar.DAY_OF_MONTH)
        val m = date.get(Calendar.MONTH) + 1
        return when (m) {
            1  -> if (d <= 20) ZodiacSign.CAPRICORN else ZodiacSign.AQUARIUS
            2  -> if (d <= 18) ZodiacSign.AQUARIUS  else ZodiacSign.PISCES
            3  -> if (d <= 20) ZodiacSign.PISCES    else ZodiacSign.ARIES
            4  -> if (d <= 20) ZodiacSign.ARIES     else ZodiacSign.TAURUS
            5  -> if (d <= 20) ZodiacSign.TAURUS    else ZodiacSign.GEMINI
            6  -> if (d <= 21) ZodiacSign.GEMINI    else ZodiacSign.CANCER
            7  -> if (d <= 22) ZodiacSign.CANCER    else ZodiacSign.LEO
            8  -> if (d <= 22) ZodiacSign.LEO       else ZodiacSign.VIRGO
            9  -> if (d <= 22) ZodiacSign.VIRGO     else ZodiacSign.LIBRA
            10 -> if (d <= 22) ZodiacSign.LIBRA     else ZodiacSign.SCORPIO
            11 -> if (d <= 21) ZodiacSign.SCORPIO   else ZodiacSign.SAGITTARIUS
            12 -> if (d <= 21) ZodiacSign.SAGITTARIUS else ZodiacSign.CAPRICORN
            else -> ZodiacSign.ARIES
        }
    }
}