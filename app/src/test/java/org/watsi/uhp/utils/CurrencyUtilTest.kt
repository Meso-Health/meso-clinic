package org.watsi.uhp.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class CurrencyUtilTest {

    @Test
    fun formatMoney_ETH() {
        assertEquals(CurrencyUtil.formatMoney(134, "ethiopia"), "1.34")
        assertEquals(CurrencyUtil.formatMoney(0, "ethiopia"), "0.00")
        assertEquals(CurrencyUtil.formatMoney(124567800, "ethiopia"), "1245678.00")
    }

    @Test
    fun formatMoney_UGX() {
        assertEquals(CurrencyUtil.formatMoney(134, "uganda"), "134")
        assertEquals(CurrencyUtil.formatMoney(0, "uganda"), "0")
        assertEquals(CurrencyUtil.formatMoney(124567800, "uganda"), "124567800")
    }

    @Test
    fun parseMoney_UGX() {
        assertEquals(CurrencyUtil.parseMoney("255", "uganda"), 255)
    }

    @Test
    fun parseMoney_ETH() {
        assertEquals(CurrencyUtil.parseMoney("25", "ethiopia"), 2500)
        assertEquals(CurrencyUtil.parseMoney("310.00", "ethiopia"), 31000)
        assertEquals(CurrencyUtil.parseMoney("125.50", "ethiopia"), 12550)
    }
}
