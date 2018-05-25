package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import kotlinx.android.synthetic.main.item_encounter_item_list.view.name
import kotlinx.android.synthetic.main.item_encounter_item_list.view.price
import kotlinx.android.synthetic.main.item_encounter_item_list.view.quantity
import org.watsi.domain.relations.EncounterItemWithBillable

class EncounterItemListItem @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    fun setEncounterItem(encounterItemRelation: EncounterItemWithBillable) {
        quantity.text = encounterItemRelation.encounterItem.quantity.toString()
        name.text = encounterItemRelation.billable.name // TODO: include dosage?
        price.text = encounterItemRelation.price().toString()
    }
}
