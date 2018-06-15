package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import kotlinx.android.synthetic.main.view_diagnosis_list_item.view.diagnosis_name
import kotlinx.android.synthetic.main.view_diagnosis_list_item.view.remove_diagnosis_btn
import org.watsi.domain.entities.Diagnosis

class DiagnosisListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setDiagnosis(diagnosis: Diagnosis, onRemoveDiagnosis: (diagnosis: Diagnosis) -> Unit) {
        diagnosis_name.text = diagnosis.description
        remove_diagnosis_btn.setOnClickListener {
            onRemoveDiagnosis(diagnosis)
        }
    }
}
