package org.watsi.uhp.fragments;

import android.databinding.DataBindingUtil;
import android.view.View;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.custom_components.DiagnosisFuzzySearchInput;
import org.watsi.uhp.databinding.FragmentDiagnosisBinding;
import org.watsi.uhp.models.Diagnosis;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.repositories.DiagnosisRepository;
import org.watsi.uhp.view_models.DiagnosesListViewModel;

import javax.inject.Inject;

public class DiagnosisFragment extends FormFragment<Encounter> {
    private DiagnosesListViewModel mDiagnosesListViewModel;
    private View mView;

    @Inject DiagnosisRepository diagnosisRepository;

    @Override
    int getTitleLabelId() {
        return R.string.diagnosis_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_diagnosis;
    }

    @Override
    public boolean isFirstStep() {
        return false;
    }

    @Override
    public void nextStep() {
        getNavigationManager().setEncounterFormFragment(mSyncableModel);
    }

    @Override
    void setUpFragment(View view) {
        mView = view;
        FragmentDiagnosisBinding binding = DataBindingUtil.bind(view);
        mDiagnosesListViewModel = new DiagnosesListViewModel(getContext(), mSyncableModel, true);
        binding.setDiagnosisListView(mDiagnosesListViewModel);


        getDiagnosisFuzzySearchInput().setDiagnosisChosenListener(this, diagnosisRepository);
    }

    public DiagnosisFuzzySearchInput getDiagnosisFuzzySearchInput() {
        return (DiagnosisFuzzySearchInput) mView.findViewById(R.id.diagnosis_fuzzy_search_input);
    }

    public void onDiagnosisChosen(Diagnosis diagnosis) {
        try {
            mSyncableModel.addDiagnosis(diagnosis);
            mDiagnosesListViewModel.notifyCountChange();
        } catch (Encounter.DuplicateDiagnosisException e) {
            Toast.makeText(getContext(), R.string.already_in_list_items, Toast.LENGTH_SHORT).show();
        }
    }
}
