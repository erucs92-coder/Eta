package fuck.andes.ui.app

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

internal object ConversationTimeLabels {
    private const val DAY_MS = 24L * 60L * 60L * 1000L

    fun label(
        timestampMillis: Long,
        nowMillis: Long = System.currentTimeMillis(),
        locale: Locale = Locale.getDefault(),
        timeZone: TimeZone = TimeZone.getDefault(),
    ): String {
        if (timestampMillis <= 0L) return recentLabel(locale)

        val nowStart = startOfDay(nowMillis, locale, timeZone)
        val targetStart = startOfDay(timestampMillis, locale, timeZone)
        val dayDelta = ((nowStart - targetStart) / DAY_MS).toInt()

        return when {
            dayDelta <= 0 -> format("HH:mm", timestampMillis, locale, timeZone)
            dayDelta == 1 -> yesterdayLabel(locale)
            dayDelta in 2..6 -> weekdayLabel(timestampMillis, locale, timeZone)
            sameYear(timestampMillis, nowMillis, locale, timeZone) ->
                format("M-d", timestampMillis, locale, timeZone)
            else -> format("yyyy-M-d", timestampMillis, locale, timeZone)
        }
    }

    fun nowLabel(locale: Locale = Locale.getDefault()): String =
        localizedLabel(locale, chinese = "现在", spanish = "Ahora", default = "Now")

    fun recentLabel(locale: Locale = Locale.getDefault()): String =
        localizedLabel(locale, chinese = "最近", spanish = "Reciente", default = "Recent")

    fun todaySectionLabel(locale: Locale = Locale.getDefault()): String =
        localizedLabel(locale, chinese = "今天", spanish = "Hoy", default = "Today")

    fun pinnedSectionLabel(locale: Locale = Locale.getDefault()): String =
        localizedLabel(locale, chinese = "置顶", spanish = "Fijado", default = "Pinned")

    fun yesterdayLabel(locale: Locale = Locale.getDefault()): String =
        localizedLabel(locale, chinese = "昨天", spanish = "Ayer", default = "Yesterday")

    fun isTodaySectionLabel(
        timeLabel: String,
        locale: Locale = Locale.getDefault(),
    ): Boolean = timeLabel == nowLabel(locale) || timeLabel == recentLabel(locale) || ":" in timeLabel

    private fun startOfDay(millis: Long, locale: Locale, timeZone: TimeZone): Long =
        Calendar.getInstance(timeZone, locale).apply {
            timeInMillis = millis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

    private fun sameYear(
        timestampMillis: Long,
        nowMillis: Long,
        locale: Locale,
        timeZone: TimeZone,
    ): Boolean {
        val now = Calendar.getInstance(timeZone, locale).apply { timeInMillis = nowMillis }
        val target = Calendar.getInstance(timeZone, locale).apply { timeInMillis = timestampMillis }
        return now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
    }

    private fun weekdayLabel(millis: Long, locale: Locale, timeZone: TimeZone): String {
        return format("EEE", millis, locale, timeZone)
    }

    private fun format(
        pattern: String,
        millis: Long,
        locale: Locale,
        timeZone: TimeZone,
    ): String =
        SimpleDateFormat(pattern, locale).also { it.timeZone = timeZone }.format(Date(millis))

    private fun localizedLabel(
        locale: Locale,
        chinese: String,
        spanish: String,
        default: String,
    ): String = when (locale.language.lowercase(Locale.ROOT)) {
        "zh" -> chinese
        "es" -> spanish
        else -> default
    }
}
