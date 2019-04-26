package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_dosage
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_name
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_price
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_quantity
import kotlinx.android.synthetic.main.item_receipt_list.view.stockout_indicator
import kotlinx.android.synthetic.main.item_receipt_list.view.stockout_negative_price
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.uhp.utils.CurrencyUtil
import java.text.NumberFormat

class ReceiptListItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(encounterItemRelations: EncounterItemWithBillableAndPrice) {
        receipt_billable_quantity.text = NumberFormat.getInstance().format(encounterItemRelations.encounterItem.quantity)
        receipt_billable_name.text = encounterItemRelations.billableWithPriceSchedule.billable.name
        receipt_billable_price.setPrice(encounterItemRelations.price(), encounterItemRelations.prevPrice())
        encounterItemRelations.billableWithPriceSchedule.billable.details()?.let { details ->
            receipt_billable_dosage.visibility = View.VISIBLE
            receipt_billable_dosage.text = details
        }
        if (encounterItemRelations.encounterItem.stockout) {
            stockout_indicator.visibility = View.VISIBLE
            stockout_negative_price.text = "-${CurrencyUtil.formatMoneyWithCurrency(context, encounterItemRelations.price())}"
        }
    }
}
