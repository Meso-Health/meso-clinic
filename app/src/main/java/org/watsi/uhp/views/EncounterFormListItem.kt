package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import kotlinx.android.synthetic.main.view_encounter_form_list_item.view.encounter_form_photo
import kotlinx.android.synthetic.main.view_encounter_form_list_item.view.remove_encounter_form_btn
import org.watsi.uhp.helpers.PhotoLoader
import org.watsi.uhp.viewmodels.EncounterFormViewModel

class EncounterFormListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setEncounterForm(
            encounterFormPhoto: EncounterFormViewModel.EncounterFormPhoto,
            onRemoveEncounterForm: (EncounterFormViewModel.EncounterFormPhoto) -> Unit
    ){
        PhotoLoader.loadPhoto(encounterFormPhoto.thumbnailPhoto.bytes, encounter_form_photo, context)

        remove_encounter_form_btn.setOnClickListener {
            onRemoveEncounterForm(encounterFormPhoto)
        }
    }
}
