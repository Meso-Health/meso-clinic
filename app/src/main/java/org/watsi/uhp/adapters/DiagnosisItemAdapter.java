package org.watsi.uhp.adapters;

import android.content.Context;
import android.databinding.DataBindingUtil;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import org.watsi.uhp.R;
import org.watsi.uhp.databinding.ItemDiagnosisBinding;
import org.watsi.uhp.view_models.DiagnosisItemViewModel;

import java.util.List;

public class DiagnosisItemAdapter extends ArrayAdapter<DiagnosisItemViewModel> {
    private List<DiagnosisItemViewModel> mDiagnoses;

    public DiagnosisItemAdapter(Context context, List<DiagnosisItemViewModel> diagnosisItems) {
        super(context, R.layout.item_diagnosis, diagnosisItems);
        mDiagnoses = diagnosisItems;
    }

    @Override
    @NonNull
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        ItemDiagnosisBinding diagnosisItemBinding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_diagnosis, parent, false);

        final DiagnosisItemViewModel diagnosisItem = mDiagnoses.get(position);

        diagnosisItemBinding.setDiagnosisItem(diagnosisItem);
        return diagnosisItemBinding.getRoot();
    }
}
