package org.watsi.uhp.view_models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.view.View;

import org.watsi.domain.entities.Diagnosis;

public class DiagnosisItemViewModel extends BaseObservable {
    private Diagnosis mDiagnosis;
    private DiagnosesListViewModel mDiagnosesListViewModel;
    private boolean mShowClearButton;

    public DiagnosisItemViewModel(Diagnosis diagnosis, @NonNull DiagnosesListViewModel diagnosesListViewModel, Boolean showClearButton) {
        mDiagnosesListViewModel = diagnosesListViewModel;
        mDiagnosis = diagnosis;
        mShowClearButton = showClearButton;
    }

    public void onClickRemove() {
        mDiagnosesListViewModel.removeDiagnosis(mDiagnosis);
    }

    @Bindable
    public String getDiagnosisDescription() {
        return mDiagnosis.getDescription();
    }

    @Bindable
    public int getShowClearButton() {
        return mShowClearButton ? View.VISIBLE : View.GONE;
    }
}
