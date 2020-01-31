package org.watsi.domain.utils

import org.junit.Assert
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.ZoneId
import org.threeten.bp.temporal.ChronoUnit

@RunWith(MockitoJUnitRunner::class)
class DateUtilsTest {

    lateinit var clock: Clock

    @Before
    fun setup() {
        clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    }

    @Test
    fun getYearsAgo() {
        val before = LocalDate.now().minus(6L, ChronoUnit.YEARS)

        assertEquals(6, DateUtils.getYearsAgo(before))
    }

    @Test
    fun dateAndTimeStrings() {
        val dateString = "21-05-2018"
        val timeString = "10:15am"
        val matchingLocalDateTime = LocalDateTime.parse("2018-05-21T10:15:30.000")

        assertEquals(dateString, DateUtils.formatLocalDate(matchingLocalDateTime.toLocalDate()))
        assertEquals(timeString, DateUtils.formatLocalTime(matchingLocalDateTime))
    }

    @Test
    fun isToday() {
        val today = Instant.now(clock)

        Assert.assertTrue(DateUtils.isToday(today, clock))

        val notToday = Instant.parse("2018-05-21T10:15:30.000Z")

        Assert.assertFalse(DateUtils.isToday(notToday, clock))
    }

    @Test
    fun getStartAndEndOfDayInstants() {
        val referenceInstant = Instant.ofEpochSecond(1567117107)
        val startAndEndOfDay = DateUtils.getStartAndEndOfDayInstants(referenceInstant, clock)

        assertEquals(Instant.ofEpochSecond(1567036800), startAndEndOfDay.first)
        assertEquals(Instant.ofEpochSecond(1567123200), startAndEndOfDay.second)
    }
}
