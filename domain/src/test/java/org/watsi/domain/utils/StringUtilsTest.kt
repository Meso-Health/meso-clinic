package org.watsi.domain.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class StringUtilsTest {
    @Test
    fun formatCardId() {
        assertEquals(StringUtils.formatCardId("RWI123456"), "RWI 123 456")
    }
}
