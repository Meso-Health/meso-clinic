package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_details
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_name
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_quantity
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.remove_line_item_btn
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.uhp.managers.KeyboardManager
import java.util.UUID

class EncounterItemListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(
            encounterItemRelation: EncounterItemWithBillable,
            onQuantitySelected: () -> Unit,
            onQuantityDeselected: () -> Unit,
            onQuantityChanged: (encounterItemId: UUID, newQuantity: Int) -> Unit,
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
                keyboardManager.hideKeyboard(v)
                onQuantityDeselected()

                val parsedQuantity = billable_quantity.text.toString().toIntOrNull()
                if (parsedQuantity == null || parsedQuantity == 0) {
                    billable_quantity.setText(currentQuantity.toString())
                    Toast.makeText(context, org.watsi.uhp.R.string.error_blank_or_zero_quantity,
                            android.widget.Toast.LENGTH_SHORT).show()
                } else {
                    if (parsedQuantity != currentQuantity) {
                        onQuantityChanged(encounterItem.id, parsedQuantity)
                    } else {
                        // always set the field text to the parsed quantity (otherwise if
                        // currentQuantity is 10 and user inputs "0010", it would stay at "0010"
                        // instead of updating to 10)
                        billable_quantity.setText(parsedQuantity.toString())
                    }
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
