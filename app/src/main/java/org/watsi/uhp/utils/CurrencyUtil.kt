package org.watsi.uhp.utils

import android.content.Context
import org.watsi.uhp.BuildConfig
import org.watsi.uhp.R
import java.math.BigDecimal
import kotlin.math.log10
import kotlin.math.roundToInt

object CurrencyUtil {
    fun formatMoneyWithCurrency(context: Context, amount: Int): String {
        return context.getString(R.string.price_with_currency, formatMoney(amount))
    }

    fun formatMoney(amount: Int, moneyMultiple: Int = BuildConfig.MONEY_MULTIPLE): String {
        // The scale is the number of digits to the right of the decimal point.
        val scale  = log10(moneyMultiple.toDouble()).toInt()
        return BigDecimal(amount).setScale(scale).divide(BigDecimal(moneyMultiple)).toString()
    }

    /**
     * Parses money String as integer value of the lowest denomination of the currency
     */
    fun parseMoney(amount: String, moneyMultiple: Int = BuildConfig.MONEY_MULTIPLE): Int {
        return BigDecimal(amount).multiply(BigDecimal(moneyMultiple)).toInt()
    }
}
