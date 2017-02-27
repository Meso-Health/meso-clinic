package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.managers.KeyboardManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.EncounterItem;

public class AddNewBillableFragment extends Fragment {

    private EditText nameField;
    private EditText priceField;
    private Button addBillableButton;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.add_new_billable_fragment_label);

        view = inflater.inflate(R.layout.fragment_add_new_billable, container, false);

        nameField = (EditText) view.findViewById(R.id.name_field);
        priceField = (EditText) view.findViewById(R.id.price_field);
        addBillableButton = (Button) view.findViewById(R.id.add_billable_button);

        KeyboardManager.focusAndForceShowKeyboard(nameField, getContext());

        setAddBillableButton();
        setNameField();
        setPriceField();

        return view;
    }

    private void setNameField() {
        nameField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int count, int after) {
                if (nameField.getText().toString().length() != 0 && priceField.getText().toString().length() != 0) {
                    addBillableButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });
    }

    private void setPriceField() {
        priceField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                // no-op
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (nameField.getText().toString().length() != 0 && priceField.getText().toString().length() != 0) {
                    addBillableButton.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                // no-op
            }
        });
    }

    private void setAddBillableButton() {
        addBillableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nameField.getText().toString().length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.empty_billable_name_field,
                            Toast.LENGTH_LONG).show();
                } else if (priceField.getText().toString().length() == 0) {
                    Toast.makeText(getActivity().getApplicationContext(), R.string.empty_billable_price_field,
                            Toast.LENGTH_LONG).show();
                } else {
                    Billable billable = new Billable();
                    billable.setName(nameField.getText().toString());
                    billable.setPrice(Integer.parseInt(priceField.getText().toString()));
                    billable.setType(Billable.TypeEnum.UNSPECIFIED);
                    billable.setCreatedDuringEncounter(true);

                    EncounterItem encounterItem = new EncounterItem();
                    encounterItem.setBillable(billable);

                    KeyboardManager.hideKeyboard(view, getContext());

                    MainActivity activity = (MainActivity) getActivity();
                    activity.getCurrentLineItems().add(encounterItem);
                    new NavigationManager(activity).setEncounterFragment();
                }
            }
        });
    }
}
