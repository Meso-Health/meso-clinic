package org.watsi.uhp.listeners;

import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;

import org.watsi.uhp.presenters.EncounterPresenter;

public class BillableSearchEncounterFragmentListener implements SearchView.OnQueryTextListener {

    private final EncounterPresenter encounterPresenter;

    public BillableSearchEncounterFragmentListener(EncounterPresenter encounterPresenter) {
        this.encounterPresenter = encounterPresenter;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.isEmpty()) {
            encounterPresenter.billableCursorAdapter = encounterPresenter.getBillableCursorAdapter(newText);
            encounterPresenter.getDrugSearchView().setSuggestionsAdapter(encounterPresenter.billableCursorAdapter);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // no-op
        return true;
    }
}
