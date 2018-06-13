package org.watsi.uhp.helpers

import org.junit.Test
import org.junit.Before
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Assert.assertFalse
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId

class DateHelperTest {

    lateinit var clock: Clock

    @Before
    fun setup() {
        clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    }

    @Test
    fun longDateString() {
        val dateString = "May 21, 2018"
        val matchingInstant = Instant.parse("2018-05-21T10:15:30.000Z")

        assertEquals(dateString, DateHelper.formatDateString(matchingInstant, clock))
    }

    @Test
    fun isToday() {
        val today = Instant.now(clock)

        assertTrue(DateHelper.isToday(today, clock))

        val notToday = Instant.parse("2018-05-21T10:15:30.000Z")

        assertFalse(DateHelper.isToday(notToday, clock))
    }
}
