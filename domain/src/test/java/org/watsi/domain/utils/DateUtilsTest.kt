package org.watsi.domain.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.LocalDate
import org.threeten.bp.temporal.ChronoUnit

@RunWith(MockitoJUnitRunner::class)
class DateUtilsTest {

    @Test
    fun getYearsAgo() {
        val before = LocalDate.now().minus(6L, ChronoUnit.YEARS)

        assertEquals(6, DateUtils.getYearsAgo(before))
    }
}
