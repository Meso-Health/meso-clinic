package org.watsi.uhp.helpers

import org.junit.Assert.assertEquals
import org.junit.Test

class LocaleHelperTest {

    @Test
    fun countryName_returnsString() {
        val expected = "Ethiopia"

        assertEquals(expected, LocaleHelper.countryName())
    }
}
