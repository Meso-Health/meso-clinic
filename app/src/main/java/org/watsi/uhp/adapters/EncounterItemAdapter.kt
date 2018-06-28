package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.uhp.R
import org.watsi.uhp.managers.KeyboardManager
import org.watsi.uhp.views.EncounterItemListItem
import java.util.UUID

class EncounterItemAdapter(
        private val encounterItems: MutableList<EncounterItemWithBillable> = mutableListOf(),
        private val onQuantitySelected: () -> Unit,
        private val onQuantityDeselected: () -> Unit,
        private val onQuantityChanged: (encounterItemId: UUID, newQuantity: Int?) -> Unit,
        private val onRemoveEncounterItem: (encounterItemId: UUID) -> Unit,
        private val keyboardManager: KeyboardManager
) : RecyclerView.Adapter<EncounterItemAdapter.ViewHolder>() {

    lateinit var encounterItemView: EncounterItemListItem

    override fun getItemCount(): Int = encounterItems.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.view_encounter_item_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val encounterItemRelation = encounterItems[position]
        encounterItemView = holder.itemView as EncounterItemListItem
        encounterItemView.setEncounterItem(encounterItemRelation, onQuantitySelected,
                onQuantityDeselected, onQuantityChanged, onRemoveEncounterItem, keyboardManager)
    }

    fun setEncounterItems(updatedEncounterItems: List<EncounterItemWithBillable>) {
        if (updatedEncounterItems != encounterItems) {
            encounterItems.clear()
            encounterItems.addAll(updatedEncounterItems)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
