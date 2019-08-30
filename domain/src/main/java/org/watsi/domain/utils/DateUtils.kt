package org.watsi.domain.utils

import org.threeten.bp.Clock
import org.threeten.bp.DateTimeException
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZonedDateTime
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.temporal.ChronoUnit
import org.watsi.domain.entities.Member

object DateUtils {
    const val TIME_FORMAT = "h:mma"
    const val DATE_FORMAT = "dd-MM-yyyy"

    fun formatLocalDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
        return localDate.format(formatter)
    }

    fun formatLocalTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern(TIME_FORMAT)
        return localDateTime.format(formatter).toLowerCase()
    }

    fun isDateInFuture(localDate: LocalDate, clock: Clock = Clock.systemUTC()): Boolean {
        return localDate.isAfter(LocalDate.now(clock))
    }

    fun isToday(instant: Instant, clock: Clock = Clock.systemUTC()): Boolean {
        val todayDate: LocalDateTime = LocalDateTime.now(clock)
        val occurredAtDate = LocalDateTime.ofInstant(instant, clock.zone)

        return todayDate.dayOfYear == occurredAtDate.dayOfYear && todayDate.year == occurredAtDate.year
    }

    fun dateWithAccuracyToAge(date: LocalDate,
                              accuracy: Member.DateAccuracy,
                              clock: Clock = Clock.systemUTC()
    ): Age? {
        return when (accuracy) {
            Member.DateAccuracy.M -> {
                Age(getMonthsAgo(date, clock), AgeUnit.months)
            }
            Member.DateAccuracy.Y -> {
                Age(getYearsAgo(date, clock), AgeUnit.years)
            }
            else -> null
        }
    }

    fun isValidBirthdate(day: Int, month: Int, year: Int, clock: Clock = Clock.systemUTC()): Boolean {
        if (!(day in 1..31 && month in 1..12 && year >= 1900)) return false
        try {
            val birthdate = LocalDate.of(year, month, day)
            if (isDateInFuture(birthdate, clock)) return false
        } catch (e: DateTimeException) {
            return false
        }
        return true
    }

    fun getMonthsAgo(date: LocalDate, clock: Clock = Clock.systemUTC()): Int {
        return ChronoUnit.MONTHS.between(date, LocalDate.now(clock)).toInt()
    }

    fun getYearsAgo(date: LocalDate, clock: Clock = Clock.systemUTC()): Int {
        return ChronoUnit.YEARS.between(date, LocalDate.now(clock)).toInt()
    }

    fun getDaysAgo(date: LocalDate, clock: Clock = Clock.systemUTC()): Int {
        return ChronoUnit.DAYS.between(date, LocalDate.now(clock)).toInt()
    }

    /**
     * This uses the zone from the supplied clock which will most likely be relative to the current
     * user's timezone. The backend duplication logic is based on the Rails application timezone,
     * so there could be cases where the returns from this function do not correspond to the
     * backend validation logic.
     *
     * TODO: Calculate the day based on the backend's timezone settings
     */
    fun getStartAndEndOfDayInstants(referenceInstant: Instant, clock: Clock): Pair<Instant, Instant> {
        val zdt = ZonedDateTime.ofInstant(referenceInstant, clock.zone)
        val beginningOfDayDateTime = zdt.toLocalDate().atStartOfDay(clock.zone)
        val beginningOfDay = beginningOfDayDateTime.toInstant()
        val endOfDay = beginningOfDayDateTime.plusDays(1).toInstant()

        return Pair(beginningOfDay, endOfDay)
    }

    fun instantToLocalDate(instant: Instant, clock: Clock): LocalDate {
        return ZonedDateTime.ofInstant(instant, clock.zone).toLocalDate()
    }
}

data class Age (val quantity: Int, val unit: AgeUnit) {

    fun toBirthdateWithAccuracy(clock: Clock = Clock.systemUTC()): Pair<LocalDate, Member.DateAccuracy> {
        return when (unit) {
            AgeUnit.days -> Pair(LocalDate.now(clock).minusDays(quantity.toLong()), Member.DateAccuracy.D)
            AgeUnit.months -> Pair(LocalDate.now(clock).minusMonths(quantity.toLong()), Member.DateAccuracy.M)
            AgeUnit.years -> Pair(LocalDate.now(clock).minusYears(quantity.toLong()), Member.DateAccuracy.Y)
        }
    }

    override fun toString() = "$quantity $unit"
}

enum class AgeUnit { months, years, days }
