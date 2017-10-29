package org.watsi.uhp.listeners;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.presenters.EncounterPresenter;

public class BillableSelectedEncounterFragmentListener implements AdapterView.OnItemSelectedListener {

    private final EncounterPresenter encounterPresenter;
    private final ArrayAdapter adapter;
    private final Context context;

    public BillableSelectedEncounterFragmentListener(EncounterPresenter encounterPresenter, ArrayAdapter adapter, Context context) {
        this.encounterPresenter = encounterPresenter;
        this.adapter = adapter;
        this.context = context;
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        if (position != 0) {
            Billable billable = (Billable) adapter.getItem(position);
            try {
                if (billable.requiresLabResult()) {
                    encounterPresenter.getLabResultSpinner().setVisibility(View.VISIBLE);
                    encounterPresenter.getLabResultSpinnerWrapper().setVisibility(View.VISIBLE);
                    encounterPresenter.getLabResultSpinner().performClick();
                } else {
                    encounterPresenter.addToEncounterItemList(billable);
                    encounterPresenter.getBillableSpinner().setSelection(0);
                    encounterPresenter.getLabResultSpinner().setVisibility(View.GONE);
                    encounterPresenter.scrollToBottom();
                }
            } catch (Encounter.DuplicateBillableException e) {
                // TODO: make toast message more descriptive
                Toast.makeText(context, R.string.already_in_list_items, Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {
        // no-op
    }
}
