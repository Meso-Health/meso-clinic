package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_details
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_name
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_quantity
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.remove_line_item_btn
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.uhp.managers.KeyboardManager
import java.text.NumberFormat
import java.util.UUID

class EncounterItemListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(
            encounterItemRelation: EncounterItemWithBillable,
            onQuantitySelected: () -> Unit,
            onQuantityChanged: (encounterItemId: UUID, newQuantity: Int?) -> Unit,
            onRemoveEncounterItem: (encounterItemId: UUID) -> Unit,
            keyboardManager: KeyboardManager
    ) {
        val billable = encounterItemRelation.billable
        val encounterItem = encounterItemRelation.encounterItem
        val currentQuantity = encounterItem.quantity

        billable_name.text = billable.name
        if (billable.dosageDetails() != null) {
            billable_details.text = billable.dosageDetails()
            billable_details.visibility = View.VISIBLE
        } else {
            billable_details.visibility = View.GONE
        }

        billable_quantity.setText(currentQuantity.toString())
        billable_quantity.isEnabled = billable.type in listOf(Billable.Type.DRUG, Billable.Type.SUPPLY, Billable.Type.VACCINE)
        billable_quantity.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) { // execute the following when losing focus
                val parsedNewQuantity = billable_quantity.text.toString().toIntOrNull()

                if (parsedNewQuantity == null || parsedNewQuantity == 0) {
                    // set field text back to previous quantity
                    billable_quantity.setText(currentQuantity.toString())
                } else {
                    // always set the field text to the parsed quantity (otherwise if
                    // currentQuantity is 10 and user inputs "0010", it would stay at "0010"
                    // instead of updating to 10)
                    billable_quantity.setText(parsedNewQuantity.toString())
                }

                if (parsedNewQuantity != currentQuantity) {
                    onQuantityChanged(encounterItem.id, parsedNewQuantity)
                }
            } else {
                onQuantitySelected()
            }
        }
        // Clear focus when the IME done checkmark is pressed. (Android does not do this automatically.)
        billable_quantity.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                v.clearFocus()
            }
            false
        }

        remove_line_item_btn.setOnClickListener { onRemoveEncounterItem(encounterItem.id) }
    }
}
