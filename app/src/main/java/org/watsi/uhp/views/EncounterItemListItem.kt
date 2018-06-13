package org.watsi.uhp.views

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnFocusChangeListener
import android.widget.RelativeLayout
import android.widget.Toast
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_details
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_name
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_quantity
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.remove_line_item_btn
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillable
import java.util.UUID

class EncounterItemListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(
            encounterItemRelation: EncounterItemWithBillable,
            onQuantityChanged: (encounterItemId: UUID, newQuantity: Int) -> Unit,
            onRemoveEncounterItem: (encounterItemId: UUID) -> Unit
    ) {
        val billable = encounterItemRelation.billable
        val encounterItem = encounterItemRelation.encounterItem

        billable_name.text = billable.name
        billable_details.text = billable.dosageDetails()

        billable_quantity.setText(encounterItem.quantity.toString())
        billable_quantity.isEnabled = billable.type in listOf(Billable.Type.DRUG, Billable.Type.SUPPLY, Billable.Type.VACCINE)
        billable_quantity.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) { // execute the following when losing focus
                if (billable_quantity.text.toString() in listOf("", "0")) {
                    billable_quantity.setText(encounterItem.quantity.toString()) // set quantity back to original
                    Toast.makeText(context, org.watsi.uhp.R.string.error_blank_or_zero_quantity,
                            android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    onQuantityChanged(encounterItem.id, billable_quantity.text.toString().toInt())
                }
            }
        }

        remove_line_item_btn.setOnClickListener { onRemoveEncounterItem(encounterItem.id) }
    }
}
