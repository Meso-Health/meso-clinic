package org.watsi.uhp.adapters

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast

import org.watsi.domain.entities.Billable
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.uhp.R

class EncounterItemAdapter(context: Context,
                           resource: Int,
                           objects: List<EncounterItemWithBillable>
) : ArrayAdapter<EncounterItemWithBillable>(context, resource, objects) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        val viewHolder: ViewHolder

        if (convertView == null) {
            val layoutInflater = (context as Activity).layoutInflater
            convertView = layoutInflater.inflate(R.layout.item_encounter_item_list, parent, false)!!

            viewHolder = ViewHolder()
            viewHolder.billableName = convertView.findViewById<View>(R.id.billable_name) as TextView
            viewHolder.billableDetails = convertView.findViewById<View>(R.id.billable_details) as TextView
            viewHolder.removeLineItemBtn = convertView.findViewById<View>(R.id.remove_line_item_btn) as Button
            viewHolder.billableQuantity = convertView.findViewById<View>(R.id.billable_quantity) as EditText

            convertView.tag = viewHolder
        } else {
            viewHolder = convertView.tag as ViewHolder
        }

        getItem(position)?.let { encounterItemWithBillable ->
            val encounterItem = encounterItemWithBillable.encounterItem
            val billable = encounterItemWithBillable.billable

            viewHolder.billableName!!.text = billable.name
            if (billable.dosageDetails() == null) {
                viewHolder.billableDetails?.visibility = View.GONE
            } else {
                viewHolder.billableDetails?.text = billable.dosageDetails()
                viewHolder.billableDetails?.visibility = View.VISIBLE
            }

            viewHolder.removeLineItemBtn?.setOnClickListener {
                remove(encounterItemWithBillable)
            }

            viewHolder.billableQuantity?.onFocusChangeListener = View.OnFocusChangeListener { _, hasFocus ->
                if (!hasFocus) {
                    val quantity = viewHolder.billableQuantity!!.text.toString()

                    if (quantity == "" || quantity == "0") {
                        viewHolder.billableQuantity!!.setText(encounterItem.quantity.toString())
                        Toast.makeText(context, "Please enter nonzero quantity", Toast.LENGTH_SHORT).show()
                    } else {
                        encounterItem.quantity = Integer.valueOf(quantity)
                    }
                }
            }
            viewHolder.billableQuantity?.setText(encounterItem.quantity.toString())

            viewHolder.billableQuantity?.isEnabled = !(billable.type == Billable.Type.SERVICE
                        || billable.type == Billable.Type.LAB)
        }

        return convertView
    }

    private class ViewHolder {
        internal var removeLineItemBtn: Button? = null
        internal var billableName: TextView? = null
        internal var billableDetails: TextView? = null
        internal var billableQuantity: EditText? = null
    }
}
