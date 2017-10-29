package org.watsi.uhp.view_models;

import android.content.Context;
import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.databinding.BindingAdapter;
import android.support.annotation.NonNull;
import android.widget.ListView;

import com.android.databinding.library.baseAdapters.BR;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.DiagnosisItemAdapter;
import org.watsi.uhp.helpers.ListViewUtils;
import org.watsi.uhp.models.Diagnosis;
import org.watsi.uhp.models.Encounter;

import java.util.ArrayList;
import java.util.List;

public class DiagnosesListViewModel extends BaseObservable {
    private Encounter mEncounter;
    private boolean mAllowDiagnosisToBeRemoved;
    Context mContext;

    public DiagnosesListViewModel(Context context, Encounter encounter, boolean allowDiagnosisToBeRemoved) {
        mEncounter = encounter;
        mAllowDiagnosisToBeRemoved = allowDiagnosisToBeRemoved;
        mContext = context;
    }

    @Bindable
    public List<DiagnosisItemViewModel> getSelectedDiagnoses() {
        return getListOfDiagnosisItemViewModelFromEncounter();
    }

    private List<DiagnosisItemViewModel> getListOfDiagnosisItemViewModelFromEncounter() {
        List<DiagnosisItemViewModel> diagnosisItemViewModelsList = new ArrayList<>();
        for (Diagnosis diagnosis: mEncounter.getDiagnoses()) {
            DiagnosisItemViewModel diagnosisItemViewModel = new DiagnosisItemViewModel(diagnosis, this, mAllowDiagnosisToBeRemoved);
            diagnosisItemViewModelsList.add(diagnosisItemViewModel);
        }
        return diagnosisItemViewModelsList;
    }

    @BindingAdapter("android:entries")
    public static void setSelectedDiagnoses(ListView listView, List<DiagnosisItemViewModel> diagnosisItems) {
        listView.setAdapter(new DiagnosisItemAdapter(listView.getContext(), diagnosisItems));
        listView.setSelection(listView.getAdapter().getCount() - 1);
        ListViewUtils.setDynamicHeight(listView);
    }

    public void removeDiagnosis(@NonNull Diagnosis diagnosis) {
        mEncounter.removeDiagnosis(diagnosis);
        notifyCountChange();
    }

    @Bindable
    public String getFormattedCount() {
        int count = mEncounter.getDiagnoses().size();
        return mContext.getResources().getQuantityString(
                R.plurals.diagnosis_count, count, count);
    }

    public void notifyCountChange() {
        notifyPropertyChanged(BR.formattedCount);
        notifyPropertyChanged(BR.selectedDiagnoses);
    }
}
