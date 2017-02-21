package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.squareup.haha.perflib.Main;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.models.Encounter;

import java.util.ArrayList;
import java.util.Arrays;

public class ClinicNumberFragment extends Fragment {
    private Spinner numberTypeSpinner;
    private EditText numberField;
    private Button submitNumberButton;
    private Encounter.ClinicNumberTypeEnum selectedNumberTypeValue;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.clinic_number_fragment_label);

        View view = inflater.inflate(R.layout.fragment_clinic_number, container, false);

        numberTypeSpinner = (Spinner) view.findViewById(R.id.clinic_number_type_dropdown);
        numberField = (EditText) view.findViewById(R.id.clinic_number_field);
        submitNumberButton = (Button) view.findViewById(R.id.clinic_number_continue_button);

        setNumberTypeSpinner();
        setNumberField();
        setSubmitNumberButton();

        return view;
    }

    private void setNumberTypeSpinner() {
        ArrayList<Object> numberTypes = new ArrayList<>();
        numberTypes.addAll(Arrays.asList(Encounter.ClinicNumberTypeEnum.values()));

        final ArrayAdapter numberTypeAdapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_spinner_dropdown_item,
                numberTypes
        );

        numberTypeSpinner.setAdapter(numberTypeAdapter);
        numberTypeSpinner.setOnItemSelectedListener(new NumberTypeListener());
    }

    private void setNumberField() {
        numberField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                //no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (numberField.getText().toString().length() != 0) {
                    submitNumberButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                //no-op
            }
        });
    }

    private void setSubmitNumberButton() {
        submitNumberButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();

                Encounter encounter = activity.getCurrentEncounter();
                encounter.setClinicNumberType(selectedNumberTypeValue);
                encounter.setClinicNumber(Integer.parseInt(numberField.getText().toString()));

                activity.setEncounterFragment();
            }
        });
    }

    private class NumberTypeListener implements AdapterView.OnItemSelectedListener {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedNumberTypeValue = (Encounter.ClinicNumberTypeEnum) parent.getItemAtPosition(position);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {
            //no-op
        }
    }
}
