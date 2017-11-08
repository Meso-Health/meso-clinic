package org.watsi.uhp.custom_components;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.DiagnosisAdapter;
import org.watsi.uhp.fragments.DiagnosisFragment;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.models.Diagnosis;

public class DiagnosisFuzzySearchInput extends LinearLayout {
    DiagnosisAdapter mDiagnosisAdapter;
    DiagnosisFragment mFragment;
    AppCompatAutoCompleteTextView mAutoCompleteTextView;
    ImageView mClearButton;

    public DiagnosisFuzzySearchInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.diagnosis_fuzzy_search_input, this);
    }

    public void setDiagnosisChosenListener(DiagnosisFragment fragment) {
        mFragment = fragment;
        mDiagnosisAdapter = new DiagnosisAdapter(getContext(), R.layout.item_diagnosis_search_suggestion, R.id.diagnosis_description);
        mAutoCompleteTextView = (AppCompatAutoCompleteTextView) findViewById(R.id.diagnosis_search);
        mClearButton = (ImageView) findViewById(R.id.diagnosis_search_clear);

        mAutoCompleteTextView.setAdapter(mDiagnosisAdapter);
        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Diagnosis diagnosis = mDiagnosisAdapter.getItem(position);
                mFragment.onDiagnosisChosen(diagnosis);
                mAutoCompleteTextView.setText("");
                KeyboardManager.hideKeyboard(DiagnosisFuzzySearchInput.this, getContext());
            }
        });
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAutoCompleteTextView.setText("");
            }
        });
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            mAutoCompleteTextView.showDropDown();
        }
    }
}
