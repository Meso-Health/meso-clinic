package org.watsi.uhp.listeners;

import android.widget.SearchView;

import org.watsi.uhp.presenters.EncounterPresenter;

public class BillableSearchEncounterFragmentListener implements SearchView.OnQueryTextListener {

    private final EncounterPresenter encounterPresenter;

    public BillableSearchEncounterFragmentListener(EncounterPresenter encounterPresenter) {
        this.encounterPresenter = encounterPresenter;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        if (!newText.isEmpty()) {
            encounterPresenter.updateBillableSearchSuggestions(newText);
        }
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        // no-op
        return true;
    }
}
