package org.watsi.uhp.helpers

import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.uhp.helpers.EthiopianDateHelper.EthiopianDate

class EthiopianDateHelperTest {
    lateinit var clock: Clock

    @Before
    fun setup() {
        clock = Clock.fixed(Instant.now(), ZoneId.of("Africa/Addis_Ababa"))
    }

    @Test
    fun formatEthiopianDate() {
        val testDate = Instant.parse("2018-09-10T10:15:30.000Z")
        val expectedString = "05 / 13 / 2010"

        assertEquals(expectedString, EthiopianDateHelper.formatEthiopianDate(testDate, clock))
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
    fun toInstant() {
        val ethDate = EthiopianDate(2010, 9, 13) // May 21st 2018 in Addis
        val expectedInstant = Instant.parse("2018-05-20T21:00:00.000Z") // 9pm May 20th GMT
        val returnedInstant = EthiopianDateHelper.toInstant(
            ethDate.year, ethDate.month, ethDate.day, 0, 0, 0, 0, clock
        )

        assertEquals(expectedInstant, returnedInstant)
    }

    @Test
    fun toEthiopianDate() {
        val instant = Instant.parse("2018-05-21T23:15:30.000Z") // 11:15pm May 21st GMT
        val expectedEthDate = EthiopianDate(2010, 9, 14) // 2:15am May 22nd Addis

        assertEquals(expectedEthDate, EthiopianDateHelper.toEthiopianDate(instant, clock))
    }
}
