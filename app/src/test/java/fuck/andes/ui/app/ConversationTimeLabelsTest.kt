package fuck.andes.ui.app

import java.util.Calendar
import java.util.Locale
import java.util.TimeZone
import org.junit.Assert.assertEquals
import org.junit.Test

class ConversationTimeLabelsTest {
    private val timeZone = TimeZone.getTimeZone("Asia/Shanghai")
    private val locale = Locale.CHINA

    @Test
    fun labelUsesClockTimeForToday() {
        val now = millis(2026, Calendar.JULY, 4, 19, 32)
        val timestamp = millis(2026, Calendar.JULY, 4, 9, 5)

        assertEquals("09:05", label(timestamp, now))
    }

    @Test
    fun labelUsesRelativeDayForRecentHistory() {
        val now = millis(2026, Calendar.JULY, 4, 19, 32)

        assertEquals("昨天", label(millis(2026, Calendar.JULY, 3, 23, 59), now))
        assertEquals("周一", label(millis(2026, Calendar.JUNE, 29, 8, 0), now))
    }

    @Test
    fun labelUsesDateForOlderHistory() {
        val now = millis(2026, Calendar.JULY, 4, 19, 32)

        assertEquals("6-20", label(millis(2026, Calendar.JUNE, 20, 8, 0), now))
        assertEquals("2025-12-31", label(millis(2025, Calendar.DECEMBER, 31, 8, 0), now))
    }

    @Test
    fun labelFallsBackForInvalidTimestamp() {
        assertEquals("最近", label(0L, millis(2026, Calendar.JULY, 4, 19, 32)))
    }

    @Test
    fun labelFollowsRequestedLocale() {
        val now = millis(2026, Calendar.JULY, 4, 19, 32, locale = Locale.US)

        assertEquals("Yesterday", label(millis(2026, Calendar.JULY, 3, 23, 59, locale = Locale.US), now, Locale.US))
        assertEquals("Mon", label(millis(2026, Calendar.JUNE, 29, 8, 0, locale = Locale.US), now, Locale.US))
        assertEquals("Recent", label(0L, now, Locale.US))
        assertEquals("Now", ConversationTimeLabels.nowLabel(Locale.US))
        assertEquals("Today", ConversationTimeLabels.todaySectionLabel(Locale.US))
        assertEquals("Pinned", ConversationTimeLabels.pinnedSectionLabel(Locale.US))
        assertEquals("Ahora", ConversationTimeLabels.nowLabel(Locale("es", "ES")))
        assertEquals("Fijado", ConversationTimeLabels.pinnedSectionLabel(Locale("es", "ES")))
    }

    private fun label(timestamp: Long, now: Long): String =
        label(timestamp, now, locale)

    private fun label(timestamp: Long, now: Long, locale: Locale): String =
        ConversationTimeLabels.label(
            timestampMillis = timestamp,
            nowMillis = now,
            locale = locale,
            timeZone = timeZone,
        )

    private fun millis(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        locale: Locale = this.locale,
    ): Long =
        Calendar.getInstance(timeZone, locale).apply {
            clear()
            set(year, month, day, hour, minute, 0)
        }.timeInMillis
}
