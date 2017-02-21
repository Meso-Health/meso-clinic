package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import org.watsi.uhp.R;
import org.watsi.uhp.models.Encounter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class ClinicNumberFragment extends Fragment {
    private Spinner numberTypeSpinner;
    private EditText numberField;
    private Button submitNumberButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.clinic_number_fragment_label);

        View view = inflater.inflate(R.layout.fragment_clinic_number, container, false);

        numberTypeSpinner = (Spinner) view.findViewById(R.id.clinic_number_type_dropdown);
        numberField = (EditText) view.findViewById(R.id.clinic_number_field);
        submitNumberButton = (Button) view.findViewById(R.id.clinic_number_continue_button);

        setNumberTypeSpinner();
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
    }

}
