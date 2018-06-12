package org.watsi.uhp.views

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_details
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_name
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_quantity
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillable
import java.util.UUID

class EncounterItemListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(
            encounterItemRelation: EncounterItemWithBillable,
            onQuantityChanged: (encounterItemId: UUID, newQuantity: String) -> Unit
    ) {
        val billable = encounterItemRelation.billable
        val encounterItem = encounterItemRelation.encounterItem

        billable_name.text = billable.name
        billable_details.text = billable.dosageDetails()

        billable_quantity.setText(encounterItem.quantity.toString())
        billable_quantity.isEnabled = billable.type in listOf(Billable.Type.DRUG, Billable.Type.SUPPLY, Billable.Type.VACCINE)
        billable_quantity.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) { // execute the following when losing focus
                onQuantityChanged(encounterItem.id, billable_quantity.text.toString())
            }
        }
    }
}
