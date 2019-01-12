package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.uhp.R
import org.watsi.uhp.viewmodels.EncounterFormViewModel
import org.watsi.uhp.views.EncounterFormListItem

class EncounterFormAdapter(
        private val encounterForms: MutableList<EncounterFormViewModel.EncounterFormPhoto> = mutableListOf(),
        private val onRemoveEncounterForm: (EncounterFormViewModel.EncounterFormPhoto) -> Unit
) : RecyclerView.Adapter<EncounterFormAdapter.ViewHolder>() {

    lateinit var encounterFormView: EncounterFormListItem
    
    override fun getItemCount(): Int = encounterForms.size
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.view_encounter_form_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val encounterForm = encounterForms[position]
        encounterFormView = holder.itemView as EncounterFormListItem
        encounterFormView.setEncounterForm(encounterForm, onRemoveEncounterForm)
    }
    
    fun setEncounterForms(updatedEncounterForms: List<EncounterFormViewModel.EncounterFormPhoto>) {
        if (updatedEncounterForms != encounterForms) {
            encounterForms.clear()
            encounterForms.addAll(updatedEncounterForms)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
