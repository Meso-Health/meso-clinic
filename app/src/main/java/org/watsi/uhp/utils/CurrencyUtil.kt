package org.watsi.uhp.utils

import org.watsi.uhp.BuildConfig
import java.math.BigDecimal

object CurrencyUtil {
    fun formatMoney(amount: Int, flavor: String = BuildConfig.FLAVOR): String {
        return when (flavor) {
            "uganda" -> { amount.toString() }
            "ethiopia" -> { BigDecimal(amount).setScale(2).divide(BigDecimal("100")).toString() }
            else -> {
                throw IllegalStateException("CurrentUtil.formatMoney called when BuildConfig.FLAVOR is not ethiopia or uganda.")
            }
        }
    }
}
