package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.uhp.R
import org.watsi.uhp.views.EncounterItemListItem
import java.util.UUID

class EncounterItemAdapter(
    private val encounterItemRelations: MutableList<EncounterItemWithBillableAndPrice> = mutableListOf(),
    private val onQuantitySelected: () -> Unit,
    private val onQuantityChanged: (encounterItemId: UUID, newQuantity: Int?) -> Unit,
    private val onRemoveEncounterItem: (encounterItemId: UUID) -> Unit,
    private val onPriceTap: ((encounterItemId: UUID) -> Unit)?
) : RecyclerView.Adapter<EncounterItemAdapter.ViewHolder>() {

    lateinit var encounterItemView: EncounterItemListItem

    override fun getItemCount(): Int = encounterItemRelations.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.view_encounter_item_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val encounterItemRelation = encounterItemRelations[position]
        encounterItemView = holder.itemView as EncounterItemListItem
        encounterItemView.setEncounterItem(encounterItemRelation, onQuantitySelected,
                onQuantityChanged, onPriceTap)
    }

    fun setEncounterItems(updatedEncounterItemPriceScheduleAnds: List<EncounterItemWithBillableAndPrice>) {
        if (updatedEncounterItemPriceScheduleAnds != encounterItemRelations) {
            encounterItemRelations.clear()
            encounterItemRelations.addAll(updatedEncounterItemPriceScheduleAnds)
            notifyDataSetChanged()
        }
    }

    fun removeAt(position: Int) {
        onRemoveEncounterItem(encounterItemRelations[position].encounterItem.id)
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
