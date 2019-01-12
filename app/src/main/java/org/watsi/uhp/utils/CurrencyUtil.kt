package org.watsi.uhp.utils

import org.watsi.uhp.BuildConfig
import java.math.BigDecimal

object CurrencyUtil {
    const val BIRR_MULT = 100

    fun formatMoney(amount: Int, flavor: String = BuildConfig.FLAVOR): String {
        return when (flavor) {
            "uganda" -> { amount.toString() }
            "ethiopia" -> { BigDecimal(amount).setScale(2).divide(BigDecimal(BIRR_MULT)).toString() }
            else -> {
                throw IllegalStateException("CurrentUtil.formatMoney called when BuildConfig.FLAVOR is not ethiopia or uganda.")
            }
        }
    }

    /**
     * Parses money String as integer value of the lowest denomination of the currency
     */
    fun parseMoney(amount: String, flavor: String = BuildConfig.FLAVOR): Int {
        return when (flavor) {
            "uganda" -> { amount.toInt() }
            "ethiopia" -> { BigDecimal(amount).multiply(BigDecimal(BIRR_MULT)).toInt() }
            else -> {
                throw IllegalStateException("CurrentUtil.parseMoney called when BuildConfig.FLAVOR is not ethiopia or uganda.")
            }
        }
    }
}
