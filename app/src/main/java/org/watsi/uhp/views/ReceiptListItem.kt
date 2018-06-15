package org.watsi.uhp.views

import android.content.Context
import android.util.AttributeSet
import android.support.constraint.ConstraintLayout
import android.view.View
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_quantity
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_name
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_price
import kotlinx.android.synthetic.main.item_receipt_list.view.receipt_billable_dosage
import org.watsi.domain.relations.EncounterItemWithBillable

class ReceiptListItem @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(encounterItemRelation: EncounterItemWithBillable) {
        receipt_billable_quantity.text = encounterItemRelation.encounterItem.quantity.toString()
        receipt_billable_name.text = encounterItemRelation.billable.name
        receipt_billable_price.text = encounterItemRelation.price().toString()
        encounterItemRelation.billable.dosageDetails()?.let { dosageDetails ->
            receipt_billable_dosage.visibility = View.VISIBLE
            receipt_billable_dosage.text = dosageDetails
        }
    }
}
