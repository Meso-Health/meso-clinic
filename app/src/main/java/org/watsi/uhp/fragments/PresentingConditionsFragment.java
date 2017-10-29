package org.watsi.uhp.fragments;

import android.databinding.DataBindingUtil;
import android.view.View;

import org.watsi.uhp.R;
import org.watsi.uhp.databinding.FragmentPresentingConditionsBinding;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.view_models.PresentingConditionsViewModel;

public class PresentingConditionsFragment extends FormFragment<Encounter> {
    View mView;
    PresentingConditionsViewModel mPresentingConditionsViewModel;

    @Override
    int getTitleLabelId() {
        return R.string.fragment_presenting_conditions;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_presenting_conditions;
    }

    @Override
    public boolean isFirstStep() {
        return true;
    }

    @Override
    public void nextStep() {
        getNavigationManager().setEncounterFragment(mSyncableModel);
    }

    @Override
    void setUpFragment(View view) {
        mView = view;
        FragmentPresentingConditionsBinding binding = DataBindingUtil.bind(view);
        mPresentingConditionsViewModel = new PresentingConditionsViewModel(mView, mSyncableModel);
        binding.setPresentingConditionsViewModel(mPresentingConditionsViewModel);
    }
}
