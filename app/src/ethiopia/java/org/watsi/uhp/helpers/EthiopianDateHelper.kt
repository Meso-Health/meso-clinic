package org.watsi.uhp.helpers

import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import org.joda.time.chrono.EthiopicChronology
import org.joda.time.format.DateTimeFormat
import org.threeten.bp.Clock
import org.threeten.bp.Instant

object EthiopianDateHelper {
    const val DATE_FORMAT = "dd-MM-yyyy"
    const val MONTHS_IN_YEAR = 13

    fun formatEthiopianDate(instant: Instant, clock: Clock): String {
        return DateTimeFormat.forPattern(DATE_FORMAT).print(toEthDateTime(instant, clock))
    }

    /**
     * Returns the number of days in the month of the year specified. If the date matches "today"
     * then it will only return the number of days up to today, excluding future days.
     */
    fun daysInMonthNotInFuture(year: Int, month: Int, todayDate: EthiopianDate): Int {
        return if (month == todayDate.month && year == todayDate.year) {
            todayDate.day
        } else {
            DateTime(EthiopicChronology.getInstance())
                .withDate(year, month, 1) // The dayOfMonth doesn't matter
                .dayOfMonth().maximumValue
        }
    }

    /**
     * Returns the number of months in the year. If the date matches "today" then it will only
     * return the number of months up to today, excluding future months.
     */
    fun monthsInYearNotInFuture(year: Int, todayDate: EthiopianDate): Int {
        return if (year == todayDate.year) {
            todayDate.month
        } else {
            MONTHS_IN_YEAR
        }
    }

    fun toInstant(
        year: Int,
        month: Int,
        day: Int,
        hour: Int,
        minute: Int,
        second: Int,
        milli: Int
    ): Instant {
        val ethDateTime = DateTime(EthiopicChronology.getInstance())
            .withZone(DateTimeZone.UTC)
            .withDate(year, month, day)
            .withTime(hour, minute, second, milli)

        return Instant.ofEpochMilli(ethDateTime.millis)
    }

    fun toEthiopianDate(instant: Instant, clock: Clock): EthiopianDate {
        val ethDate = toEthDateTime(instant, clock)
        return EthiopianDate(ethDate.year, ethDate.monthOfYear, ethDate.dayOfMonth)
    }

    private fun toEthDateTime(instant: Instant, clock: Clock): DateTime {
        return DateTime(EthiopicChronology.getInstance())
            .withZone(DateTimeZone.forID(clock.zone.id))
            .withMillis(instant.toEpochMilli())
    }

    data class EthiopianDate(val year: Int, val month: Int, val day: Int)
}
