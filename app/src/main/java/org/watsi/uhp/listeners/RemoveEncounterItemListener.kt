package org.watsi.uhp.listeners

import android.view.View
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem

import org.watsi.uhp.adapters.EncounterItemAdapter

class RemoveEncounterItemListener(
        private val mEncounter: Encounter,
        private val mEncounterItem: EncounterItem,
        private val mEncounterItemAdapter: EncounterItemAdapter) : View.OnClickListener {

    override fun onClick(v: View) {
        mEncounterItemAdapter.remove(mEncounterItem)
        mEncounter.removeEncounterItem(mEncounterItem)
    }
}
