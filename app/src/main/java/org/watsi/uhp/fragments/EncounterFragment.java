package org.watsi.uhp.fragments;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.presenters.EncounterPresenter;

import java.util.ArrayList;

public class EncounterFragment extends FormFragment<Encounter> {

    private EncounterItemAdapter encounterItemAdapter;
    private EncounterPresenter encounterPresenter;

    @Override
    int getTitleLabelId() {
        return R.string.encounter_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_encounter;
    }

    @Override
    public boolean isFirstStep() {
        return true;
    }

    @Override
    void nextStep(View view) {
        getNavigationManager().setEncounterFormFragment(mSyncableModel);
    }

    @Override
    void setUpFragment(View view) {
        encounterItemAdapter = new EncounterItemAdapter(getContext(), new ArrayList<>(mSyncableModel.getEncounterItems()));
        encounterPresenter = new EncounterPresenter(mSyncableModel, view, getContext(), encounterItemAdapter, getActivity(), this);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        encounterPresenter.setUp();
    }

    protected Encounter getEncounter() {
        return mSyncableModel;
    }

    public void updateBackdateLinkText() {
        SpannableString newText = new SpannableString(encounterPresenter.newDateLinkText(mSyncableModel));
        newText.setSpan(new UnderlineSpan(), 0, newText.length(), 0);
        encounterPresenter.getBackdateEncounterLink().setText(newText);
    }
}
