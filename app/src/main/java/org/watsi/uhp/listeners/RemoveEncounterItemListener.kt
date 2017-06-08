package org.watsi.uhp.listeners

import android.view.View

import org.watsi.uhp.adapters.EncounterItemAdapter
import org.watsi.uhp.models.Encounter
import org.watsi.uhp.models.EncounterItem

class RemoveEncounterItemListener(
        private val mEncounter: Encounter,
        private val mEncounterItem: EncounterItem,
        private val mEncounterItemAdapter: EncounterItemAdapter) : View.OnClickListener {

    override fun onClick(v: View) {
        mEncounterItemAdapter.remove(mEncounterItem)
        mEncounter.removeEncounterItem(mEncounterItem)
    }
}
