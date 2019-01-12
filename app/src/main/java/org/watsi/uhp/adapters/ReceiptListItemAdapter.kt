package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.uhp.R.layout.item_receipt_list
import org.watsi.uhp.views.ReceiptListItem

class ReceiptListItemAdapter(
        private val encounterItemRelations: List<EncounterItemWithBillableAndPrice>
) : RecyclerView.Adapter<ReceiptListItemAdapter.ViewHolder>() {

    override fun getItemCount(): Int = encounterItemRelations.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            item_receipt_list, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val encounterItem = encounterItemRelations[position]
        val view = holder.itemView as ReceiptListItem
        view.setEncounterItem(encounterItem)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
