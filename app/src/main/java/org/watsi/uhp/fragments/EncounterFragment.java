package org.watsi.uhp.fragments;

import android.support.v4.app.Fragment;
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
        encounterPresenter = new EncounterPresenter(mSyncableModel, view, getContext(), encounterItemAdapter);

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        encounterPresenter.setUpEncounterPresenter(getActivity(), getContext());

        setBackdateEncounterListener();
    }

    protected Encounter getEncounter() {
        return mSyncableModel;
    }

    private void setBackdateEncounterListener() {
        final Fragment fragment = this;
        encounterPresenter.getBackdateEncounterLink().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BackdateEncounterDialogFragment dialog = new BackdateEncounterDialogFragment();
                dialog.setTargetFragment(fragment, 0);
                dialog.show(getActivity().getSupportFragmentManager(), "BackdateEncounterDialogFragment");
            }
        });
    }

    public void updateBackdateLinkText() {
        SpannableString newText = new SpannableString(encounterPresenter.newDateLinkText(mSyncableModel));
        newText.setSpan(new UnderlineSpan(), 0, newText.length(), 0);
        encounterPresenter.getBackdateEncounterLink().setText(newText);
    }
}
