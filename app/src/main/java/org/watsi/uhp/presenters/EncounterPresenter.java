package org.watsi.uhp.presenters;

import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import java.text.SimpleDateFormat;
import java.util.Date;

public class EncounterPresenter {

    protected final Encounter mEncounter;
    protected final EncounterItemAdapter mEncounterItemAdapter;

    public EncounterPresenter(Encounter encounter, EncounterItemAdapter encounterItemAdapter) {
        mEncounter = encounter;
        mEncounterItemAdapter = encounterItemAdapter;
    }

    public void addToEncounterItemList(Billable billable) throws Encounter.DuplicateBillableException {
        EncounterItem encounterItem = new EncounterItem();
        encounterItem.setBillable(billable);

        mEncounter.addEncounterItem(encounterItem);
        mEncounterItemAdapter.add(encounterItem);
    }

    public String newDateLinkText(Encounter encounter) {
        Date backdate = encounter.getOccurredAt();
        String newBackdateText = dateFormatter(backdate);

        return "Date: " + newBackdateText;
    }

    public String dateFormatter(Date date) {
        return new SimpleDateFormat("MMM d, H:mma").format(date);
    }
}
