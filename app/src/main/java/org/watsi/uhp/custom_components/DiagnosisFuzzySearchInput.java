package org.watsi.uhp.custom_components;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.DiagnosisAdapter;
import org.watsi.uhp.fragments.DiagnosisFragment;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.Diagnosis;

public class DiagnosisFuzzySearchInput extends AppCompatAutoCompleteTextView {
    DiagnosisAdapter mDiagnosisAdapter;
    DiagnosisFragment mFragment;

    public DiagnosisFuzzySearchInput(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setDiagnosisChosenListener(DiagnosisFragment fragment) {
        mFragment = fragment;
        mDiagnosisAdapter = new DiagnosisAdapter(getContext(), R.layout.item_diagnosis_search_suggestion, R.id.diagnosis_description);
        setAdapter(mDiagnosisAdapter);
        setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Diagnosis diagnosis = mDiagnosisAdapter.getItem(position);
                mFragment.onDiagnosisChosen(diagnosis);
                setText("");
                KeyboardManager.hideKeyboard(DiagnosisFuzzySearchInput.this, getContext());
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            showDropDown();
        }
    }
}
