package org.watsi.uhp.listeners;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.LabResult;
import org.watsi.uhp.presenters.EncounterPresenter;

public class LabResultSelectedEncounterFragmentListener implements AdapterView.OnItemSelectedListener {

    private final EncounterPresenter encounterPresenter;
    private final ArrayAdapter adapter;
    private final Context context;

    public LabResultSelectedEncounterFragmentListener(EncounterPresenter encounterPresenter, ArrayAdapter adapter, Context context) {
        this.encounterPresenter = encounterPresenter;
        this.adapter = adapter;
        this.context = context;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            String labResult = (String) adapter.getItem(position);
            Billable billable = (Billable) encounterPresenter.getBillableSpinner().getSelectedItem();
            try {
                encounterPresenter.addToEncounterItemList(billable, LabResult.LabResultEnum.fromString(labResult));
                encounterPresenter.scrollToBottom();
                encounterPresenter.clearLabResult();
                encounterPresenter.getBillableSpinner().setSelection(0);
            } catch (Encounter.DuplicateBillableException e) {
                Toast.makeText(context, "You've already added " + billable.getName() + " to this encounter.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // no-op
    }
}
