package org.watsi.domain.utils

import org.threeten.bp.Clock
import org.threeten.bp.DateTimeException
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.FormatStyle
import org.threeten.bp.temporal.ChronoUnit
import org.watsi.domain.entities.Member
import java.util.Locale

object DateUtils {
    const val TIME_FORMAT = "h:mma"
    const val DAY_OF_WEEK_FORMAT = "EEEE"
    const val DATE_FORMAT = "MMM dd, yyyy"

    fun formatInstant(instant: Instant,
                      format: String,
                      zoneId: ZoneId = ZoneId.systemDefault()
    ): String {
        val formatter = DateTimeFormatter.ofPattern(format).withZone(zoneId)
        return formatter.format(instant)
    }

    fun formatInstantStyleLong(instant: Instant, clock: Clock = Clock.systemDefaultZone()): String {
        val formatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG)
            .withLocale(Locale.getDefault()).withZone(clock.zone)
        return formatter.format(instant)
    }

    fun formatLocalDate(localDate: LocalDate): String {
        val formatter = DateTimeFormatter.ofPattern(DATE_FORMAT)
        return localDate.format(formatter)
    }

    fun isDateInFuture(localDate: LocalDate, clock: Clock = Clock.systemDefaultZone()): Boolean {
        return localDate.isAfter(LocalDate.now(clock))
    }

    fun isToday(instant: Instant, clock: Clock = Clock.systemDefaultZone()): Boolean {
        val todayDate: LocalDateTime = LocalDateTime.now(clock)
        val occurredAtDate = LocalDateTime.ofInstant(instant, clock.zone)

        return todayDate.dayOfYear == occurredAtDate.dayOfYear && todayDate.year == occurredAtDate.year
    }

    fun dateWithAccuracyToAge(date: LocalDate,
                              accuracy: Member.DateAccuracy,
                              clock: Clock = Clock.systemDefaultZone()
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

    fun isValidBirthdate(day: Int, month: Int, year: Int, clock: Clock = Clock.systemDefaultZone()): Boolean {
        if (!(day in 1..31 && month in 1..12 && year >= 1900)) return false
        try {
            val birthdate = LocalDate.of(year, month, day)
            if (isDateInFuture(birthdate, clock)) return false
        } catch (e: DateTimeException) {
            return false
        }
        return true
    }

    fun getMonthsAgo(date: LocalDate, clock: Clock = Clock.systemDefaultZone()): Int {
        return ChronoUnit.MONTHS.between(date, LocalDate.now(clock)).toInt()
    }

    fun getYearsAgo(date: LocalDate, clock: Clock = Clock.systemDefaultZone()): Int {
        return ChronoUnit.YEARS.between(date, LocalDate.now(clock)).toInt()
    }

    fun getSecondsAgo(time: Long, clock: Clock = Clock.systemDefaultZone()): Double {
        return (Instant.now(clock).toEpochMilli() - time) / 1000.0
    }
}

data class Age (val quantity: Int, val unit: AgeUnit) {

    fun toBirthdateWithAccuracy(clock: Clock = Clock.systemDefaultZone()): Pair<LocalDate, Member.DateAccuracy> {
        return when (unit) {
            AgeUnit.months -> Pair(LocalDate.now(clock).minusMonths(quantity.toLong()), Member.DateAccuracy.M)
            AgeUnit.years -> Pair(LocalDate.now(clock).minusYears(quantity.toLong()), Member.DateAccuracy.Y)
        }
    }

    override fun toString() = "$quantity $unit"
}

enum class AgeUnit { months, years }
