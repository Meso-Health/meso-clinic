package org.watsi.uhp.views

import android.content.Context
import android.graphics.Paint
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.view_price_change.view.prev_price
import kotlinx.android.synthetic.main.view_price_change.view.price
import org.watsi.uhp.R
import org.watsi.uhp.utils.CurrencyUtil

class PriceChange @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_price_change, this, true)
    }

    fun setPrice(currentPrice: Int, prevPrice: Int? = null) {
        price.text = CurrencyUtil.formatMoney(currentPrice)

        if (prevPrice != null && prevPrice != currentPrice) {
            prev_price.text = context.getString(
                    R.string.prev_price, CurrencyUtil.formatMoney(prevPrice))
            prev_price.visibility = View.VISIBLE
        } else {
            prev_price.visibility = View.GONE
        }
    }

    fun underline() {
        price.paintFlags = price.paintFlags or Paint.UNDERLINE_TEXT_FLAG
    }
}
