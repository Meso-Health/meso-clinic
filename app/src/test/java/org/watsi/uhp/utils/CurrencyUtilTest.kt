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
}
