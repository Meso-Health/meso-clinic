package org.watsi.uhp.adapters

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.watsi.domain.entities.Diagnosis
import org.watsi.uhp.R
import org.watsi.uhp.views.DiagnosisListItem

class DiagnosisAdapter(
        private val onRemoveDiagnosis: (diagnosis: Diagnosis) -> Unit,
        private val diagnoses: MutableList<Diagnosis> = mutableListOf()
) : RecyclerView.Adapter<DiagnosisAdapter.DiagnosisViewHolder>() {

    lateinit var diagnosisListItemView: DiagnosisListItem
    override fun getItemCount(): Int = diagnoses.size

    override fun onBindViewHolder(holder: DiagnosisViewHolder, position: Int) {
        val diagnosis = diagnoses[position]
        diagnosisListItemView = holder.itemView as DiagnosisListItem
        diagnosisListItemView.setDiagnosis(diagnosis, onRemoveDiagnosis)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiagnosisViewHolder {
        val diagnosisView = LayoutInflater.from(parent.context).inflate(
                R.layout.view_diagnosis_list_item, parent, false)
        return DiagnosisViewHolder(diagnosisView)
    }

    fun setDiagnoses(updatedDiagnoses: List<Diagnosis>) {
        if (updatedDiagnoses != diagnoses) {
            diagnoses.clear()
            diagnoses.addAll(updatedDiagnoses)
            notifyDataSetChanged()
        }
    }

    class DiagnosisViewHolder(diagnosisView: View) : RecyclerView.ViewHolder(diagnosisView)
}
