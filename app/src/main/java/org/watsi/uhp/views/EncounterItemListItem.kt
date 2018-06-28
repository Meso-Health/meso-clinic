package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
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
            onQuantityDeselected: () -> Unit,
            onQuantityChanged: (encounterItemId: UUID, newQuantity: Int?) -> Unit,
            onRemoveEncounterItem: (encounterItemId: UUID) -> Unit,
            keyboardManager: KeyboardManager
    ) {
        val billable = encounterItemRelation.billable
        val encounterItem = encounterItemRelation.encounterItem
        val currentQuantity = encounterItem.quantity

        billable_name.text = billable.name
        billable_details.text = billable.dosageDetails()

        billable_quantity.setText(NumberFormat.getInstance().format(currentQuantity))
        billable_quantity.isEnabled = billable.type in listOf(Billable.Type.DRUG, Billable.Type.SUPPLY, Billable.Type.VACCINE)
        billable_quantity.onFocusChangeListener = OnFocusChangeListener { v, hasFocus ->
            if (!hasFocus) { // execute the following when losing focus
                onQuantityDeselected()
                keyboardManager.hideKeyboard(v)

                val newQuantity = billable_quantity.text.toString().toIntOrNull()
                if (newQuantity != currentQuantity) {
                    if (newQuantity == null || newQuantity == 0) {
                        billable_quantity.setText(currentQuantity.toString())
                    }
                    onQuantityChanged(encounterItem.id, newQuantity)
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
