package org.watsi.uhp.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CurrencyUtilTest {

    @Test
    fun formatMoney() {
        assertEquals(CurrencyUtil.formatMoney(134, 100), "1.34")
        assertEquals(CurrencyUtil.formatMoney(0, 100), "0.00")
        assertEquals(CurrencyUtil.formatMoney(124567800, 100), "1245678.00")

        assertEquals(CurrencyUtil.formatMoney(134, 1), "134")
        assertEquals(CurrencyUtil.formatMoney(0, 1), "0")
        assertEquals(CurrencyUtil.formatMoney(124567800, 1), "124567800")
    }

    @Test
    fun parseMoney() {
        assertEquals(CurrencyUtil.parseMoney("255", 1), 255)

        assertEquals(CurrencyUtil.parseMoney("25", 100), 2500)
        assertEquals(CurrencyUtil.parseMoney("310.00", 100), 31000)
        assertEquals(CurrencyUtil.parseMoney("125.50", 100), 12550)
    }
}
