package org.watsi.uhp.helpers

import org.junit.Assert.assertEquals
import org.junit.Test
import org.threeten.bp.LocalDate
import org.watsi.uhp.helpers.EthiopianDateHelper.EthiopianDate

class EthiopianDateHelperTest {

    @Test
    fun formatAsEthiopianDate() {
        val gregorianDate = LocalDate.of(2018, 9, 6)
        val expectedString = "01-13-2010"

        assertEquals(expectedString, EthiopianDateHelper.formatAsEthiopianDate(gregorianDate))
    }

    @Test
    fun daysInMonthNotInFuture() {
        val fullMonth = 3
        val intercalaryMonth = 13
        val thisMonth = 9
        val nonLeapYear = 2009
        val leapYear = 2007
        val thisYear = 2010
        val expectedFullMonth = 30
        val expectedIntercalaryMonth = 5
        val expectedIntercalaryLeapYear = 6
        val expectedThisMonth = 13
        val todayDate = EthiopianDate(thisYear, thisMonth, expectedThisMonth)

        assertEquals(expectedFullMonth,
            EthiopianDateHelper.daysInMonthNotInFuture(nonLeapYear, fullMonth, todayDate))
        assertEquals(expectedIntercalaryMonth,
            EthiopianDateHelper.daysInMonthNotInFuture(nonLeapYear, intercalaryMonth, todayDate))
        assertEquals(expectedIntercalaryLeapYear,
            EthiopianDateHelper.daysInMonthNotInFuture(leapYear, intercalaryMonth, todayDate))
        assertEquals(expectedThisMonth,
            EthiopianDateHelper.daysInMonthNotInFuture(thisYear, thisMonth, todayDate))
    }

    @Test
    fun monthsInYearNotInFuture() {
        val pastYear = 2009
        val thisYear = 2010
        val expectedPastYear = 13
        val expectedThisYear = 9
        val todayDate = EthiopianDate(thisYear, expectedThisYear, 1)

        assertEquals(expectedPastYear,
            EthiopianDateHelper.monthsInYearNotInFuture(pastYear, todayDate))
        assertEquals(expectedThisYear,
            EthiopianDateHelper.monthsInYearNotInFuture(thisYear, todayDate))
    }

    @Test
    fun toEthiopianDate() {
        val gregorianDate = LocalDate.of(2018, 9, 6)
        val ethiopianDate = EthiopianDate(2010, 13, 1)

        assertEquals(ethiopianDate, EthiopianDateHelper.toEthiopianDate(gregorianDate))
    }

    @Test
    fun fromEthiopianDate() {
        val gregorianDate = LocalDate.of(2018, 9, 6)
        val ethiopianDate = EthiopianDate(2010, 13, 1)

        assertEquals(gregorianDate, EthiopianDateHelper.fromEthiopianDate(ethiopianDate))
    }
}
