package org.watsi.uhp.helpers

import org.joda.time.DateTime
import org.joda.time.chrono.EthiopicChronology
import org.joda.time.chrono.GregorianChronology
import org.threeten.bp.LocalDate

object EthiopianDateHelper {
    const val MONTHS_IN_YEAR = 13

    fun formatAsEthiopianDate(gregorianDate: LocalDate): String {
        val ethiopianDate = toEthiopianDate(gregorianDate)
        return "${"%02d".format(ethiopianDate.day)}-${"%02d".format(ethiopianDate.month)}-${ethiopianDate.year}"
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

    fun toEthiopianDate(gregorianDate: LocalDate): EthiopianDate {
        val ethiopianDate = DateTime(GregorianChronology.getInstanceUTC())
                .withDate(gregorianDate.year, gregorianDate.monthValue, gregorianDate.dayOfMonth)
                .withChronology(EthiopicChronology.getInstanceUTC())

        return EthiopianDate(ethiopianDate.year, ethiopianDate.monthOfYear, ethiopianDate.dayOfMonth)
    }

    fun fromEthiopianDate(ethiopianDate: EthiopianDate): LocalDate {
        val gregorianDate = DateTime(EthiopicChronology.getInstanceUTC())
                .withDate(ethiopianDate.year, ethiopianDate.month, ethiopianDate.day)
                .withChronology(GregorianChronology.getInstanceUTC())

        return LocalDate.of(
            gregorianDate.year,
            gregorianDate.monthOfYear,
            gregorianDate.dayOfMonth
        )
    }

    data class EthiopianDate(val year: Int, val month: Int, val day: Int)
}
