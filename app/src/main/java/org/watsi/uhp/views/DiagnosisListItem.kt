package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import kotlinx.android.synthetic.main.view_diagnosis_list_item.view.diagnosis_name
import org.watsi.domain.entities.Diagnosis

class DiagnosisListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setDiagnosis(diagnosis: Diagnosis) {
        diagnosis_name.text = diagnosis.description
    }
}
