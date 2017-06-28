package org.watsi.uhp.listeners;

import android.content.Context;
import android.widget.SearchView;

import org.watsi.uhp.presenters.EncounterPresenter;

public class SuggestionClickEncounterFragmentListener implements SearchView.OnSuggestionListener {

    private final EncounterPresenter encounterPresenter;
    private final Context context;


    public SuggestionClickEncounterFragmentListener(EncounterPresenter encounterPresenter, Context context) {
        this.encounterPresenter = encounterPresenter;
        this.context = context;
    }

    @Override
    public boolean onSuggestionSelect(int position) {
        // no-op
        return true;
    }

    @Override
    public boolean onSuggestionClick(int position) {
        encounterPresenter.updateEncounterFromOnSuggestionClick(position);

        return true;
    }
}
