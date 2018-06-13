package org.watsi.uhp.helpers

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import java.util.Locale

object DateHelper {
    fun formatDateString(instant: Instant, clock: Clock): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.getDefault()).withZone(clock.zone)
        return formatter.format(instant)
    }

    fun isToday(instant: Instant, clock: Clock): Boolean {
        val todayDate: LocalDateTime = LocalDateTime.now(clock)
        val occurredAtDate = LocalDateTime.ofInstant(instant, clock.zone)

        return todayDate.dayOfYear == occurredAtDate.dayOfYear && todayDate.year == occurredAtDate.year
    }
}
