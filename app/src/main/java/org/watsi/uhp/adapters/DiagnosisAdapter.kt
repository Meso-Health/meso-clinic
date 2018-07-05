package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.entities.Diagnosis
import org.watsi.uhp.R
import org.watsi.uhp.views.DiagnosisListItem

class DiagnosisAdapter(
        private val diagnoses: MutableList<Diagnosis> = mutableListOf(),
        private val onRemoveDiagnosis: (diagnosis: Diagnosis) -> Unit
) : RecyclerView.Adapter<DiagnosisAdapter.ViewHolder>() {

    lateinit var diagnosisListItemView: DiagnosisListItem

    override fun getItemCount(): Int = diagnoses.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
                R.layout.view_diagnosis_list_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val diagnosis = diagnoses[position]
        diagnosisListItemView = holder.itemView as DiagnosisListItem
        diagnosisListItemView.setDiagnosis(diagnosis, onRemoveDiagnosis)
    }

    fun setDiagnoses(updatedDiagnoses: List<Diagnosis>) {
        if (updatedDiagnoses != diagnoses) {
            diagnoses.clear()
            diagnoses.addAll(updatedDiagnoses)
            notifyDataSetChanged()
        }
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view)
}
