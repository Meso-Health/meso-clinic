package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.uhp.R.layout.item_encounter_item_list
import org.watsi.uhp.views.EncounterItemListItem

class EncounterItemAdapter(private val encounterItems: List<EncounterItemWithBillable>)
    : RecyclerView.Adapter<EncounterItemAdapter.ViewHolder>() {

    override fun getItemCount(): Int = encounterItems.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val encounterItem = encounterItems[position]
        val view = holder.itemView as EncounterItemListItem
        view.setEncounterItem(encounterItem)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                item_encounter_item_list, parent, false)
        return ViewHolder(view)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
