package org.watsi.uhp.custom_components;

import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatAutoCompleteTextView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
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
    Button mClearButton;

    public DiagnosisFuzzySearchInput(Context context, AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.diagnosis_fuzzy_search_input, this);
    }

    public void setDiagnosisChosenListener(DiagnosisFragment fragment) {
        mFragment = fragment;
        mAutoCompleteTextView = (AppCompatAutoCompleteTextView) findViewById(R.id.diagnosis_search);
        mClearButton = (Button) findViewById(R.id.diagnosis_search_clear);
        mDiagnosisAdapter = new DiagnosisAdapter(getContext(), R.layout.item_diagnosis_search_suggestion, R.id.diagnosis_description);

        mAutoCompleteTextView.setAdapter(mDiagnosisAdapter);
        mAutoCompleteTextView.addTextChangedListener(new TextWatcher() {
             @Override
             public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                 // no-op
             }

             @Override
             public void onTextChanged(CharSequence s, int start, int before, int count) {
                 // no-op
             }

             @Override
             public void afterTextChanged(Editable s) {
                 if (mAutoCompleteTextView.getText().toString().isEmpty()) {
                     mClearButton.setVisibility(View.INVISIBLE);
                 } else {
                     mClearButton.setVisibility(View.VISIBLE);
                 }
             }
        });
        mAutoCompleteTextView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Diagnosis diagnosis = mDiagnosisAdapter.getItem(position);
                mFragment.onDiagnosisChosen(diagnosis);
                clearInputs();
                KeyboardManager.hideKeyboard(DiagnosisFuzzySearchInput.this, getContext());
            }
        });
        mClearButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                clearInputs();
            }
        });
    }

    public void clearInputs() {
        mAutoCompleteTextView.setText("");
        mClearButton.setVisibility(View.INVISIBLE);
    }

    @Override
    protected void onFocusChanged(boolean focused, int direction, Rect previouslyFocusedRect) {
        super.onFocusChanged(focused, direction, previouslyFocusedRect);
        if (focused) {
            mAutoCompleteTextView.showDropDown();
        }
    }
}
