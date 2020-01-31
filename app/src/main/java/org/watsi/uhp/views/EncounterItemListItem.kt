package org.watsi.uhp.views

import android.content.Context
import android.graphics.Paint
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.View
import android.view.View.OnFocusChangeListener
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_details
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_name
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.billable_quantity
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.lab_result
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.line_item_price
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.stockout_indicator
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.stockout_negative_price
import kotlinx.android.synthetic.main.view_encounter_item_list_item.view.surgical_score
import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.uhp.R
import org.watsi.uhp.utils.CurrencyUtil
import java.util.UUID

class EncounterItemListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(
        encounterItemRelation: EncounterItemWithBillableAndPrice,
        onQuantitySelected: () -> Unit,
        onQuantityChanged: (encounterItemId: UUID, newQuantity: Int?) -> Unit,
        onPriceTap: ((encounterItemId: UUID) -> Unit)?,
        onSurgicalScoreTap: ((encounterItemId: UUID) -> Unit)?
    ) {
        val billable = encounterItemRelation.billableWithPriceSchedule.billable
        val encounterItem = encounterItemRelation.encounterItem
        val labResult = encounterItemRelation.labResult
        val currentQuantity = encounterItem.quantity

        billable_name.text = billable.name
        if (billable.details() != null) {
            billable_details.text = billable.details()
            billable_details.visibility = View.VISIBLE
        } else {
            billable_details.visibility = View.GONE
        }

        if (labResult != null) {
            lab_result.text = labResult.result
            lab_result.visibility = View.VISIBLE
        } else {
            lab_result.visibility = View.GONE
        }

        billable_quantity.setText(currentQuantity.toString())
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

        line_item_price.setPrice(encounterItemRelation.price(), encounterItemRelation.prevPrice())

        onPriceTap?.let {
            line_item_price.underline()
            line_item_price.setOnClickListener { onPriceTap(encounterItem.id) }
        }

        onSurgicalScoreTap?.let {
            surgical_score.setOnClickListener { onSurgicalScoreTap(encounterItem.id) }
        }

        if (encounterItem.stockout) {
            stockout_indicator.visibility = View.VISIBLE
            stockout_negative_price.text = "-${CurrencyUtil.formatMoneyWithCurrency(context, encounterItemRelation.price())}"
        } else {
            stockout_indicator.visibility = View.GONE
            stockout_negative_price.text = null
        }

        if (onSurgicalScoreTap != null && billable.type == Billable.Type.SURGERY) {
            val text = encounterItem.surgicalScore?.let {
                context.getString(
                    R.string.surgical_score_set,
                    encounterItem.surgicalScore.toString()
                )
            }?: run {
                context.getString(
                    R.string.surgical_score
                )
            }
            surgical_score.text = text
            surgical_score.paintFlags = Paint.UNDERLINE_TEXT_FLAG
            surgical_score.visibility = View.VISIBLE

        } else {
            surgical_score.visibility = View.GONE
        }
    }
}
