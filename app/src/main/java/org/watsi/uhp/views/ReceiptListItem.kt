package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_dosage
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_name
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_price
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_quantity
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.uhp.utils.CurrencyUtil
import java.text.NumberFormat

class ReceiptListItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(encounterItemRelation: EncounterItemWithBillable) {
        receipt_billable_quantity.text = NumberFormat.getInstance().format(encounterItemRelation.encounterItem.quantity)
        receipt_billable_name.text = encounterItemRelation.billable.name
        receipt_billable_price.text = CurrencyUtil.formatMoney(encounterItemRelation.price())
        encounterItemRelation.billable.details()?.let { details ->
            receipt_billable_dosage.visibility = View.VISIBLE
            receipt_billable_dosage.text = details
        }
    }
}
