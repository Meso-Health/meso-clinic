package org.watsi.domain.utils

import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

object DateUtils {

    fun getYearsAgo(date: LocalDate, clock: Clock = Clock.systemDefaultZone()): Int {
        return ChronoUnit.YEARS.between(date, LocalDate.now(clock)).toInt()
    }
}
