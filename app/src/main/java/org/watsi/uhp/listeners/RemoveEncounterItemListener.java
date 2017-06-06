package org.watsi.uhp.listeners;

import android.view.View;

import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

public class RemoveEncounterItemListener implements View.OnClickListener {

    private final Encounter mEncounter;
    private final EncounterItem mEncounterItem;
    private final EncounterItemAdapter mEncounterItemAdapter;

    public RemoveEncounterItemListener(
            Encounter encounter, EncounterItem encounterItem, EncounterItemAdapter adapter) {
        this.mEncounter = encounter;
        this.mEncounterItem = encounterItem;
        this.mEncounterItemAdapter = adapter;
    }

    @Override
    public void onClick(View v) {
        mEncounterItemAdapter.remove(mEncounterItem);
        mEncounter.removeEncounterItem(mEncounterItem);
    }
}
