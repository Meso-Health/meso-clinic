package org.watsi.uhp.fragments;

import android.text.SpannableString;
import android.text.style.UnderlineSpan;
import android.view.View;
import android.view.WindowManager;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ClinicActivity;
import org.watsi.uhp.adapters.EncounterItemAdapter;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.presenters.EncounterPresenter;

public class EncounterFragment extends FormFragment<Encounter> {

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
        return false;
    }

    @Override
    public void nextStep() {
        getNavigationManager().setDiagnosisFragment(mSyncableModel);
    }

    @Override
    void setUpFragment(View view) {
        EncounterItemAdapter encounterItemAdapter =
                new EncounterItemAdapter(getContext(), mSyncableModel);

        encounterPresenter = new EncounterPresenter(
                mSyncableModel, view, getContext(), encounterItemAdapter,
                ((ClinicActivity) getActivity()).getNavigationManager(), this);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        encounterPresenter.setUp();
    }

    protected Encounter getEncounter() {
        return mSyncableModel;
    }

    public void updateBackdateLinkText() {
        encounterPresenter.setFormattedBackDate();
        SpannableString newText = new SpannableString("Date: " + encounterPresenter.mFormattedBackDate);
        newText.setSpan(new UnderlineSpan(), 0, newText.length(), 0);
        encounterPresenter.getBackdateEncounterLink().setText(newText);
    }
}
