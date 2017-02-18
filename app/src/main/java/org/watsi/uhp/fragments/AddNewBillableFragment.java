package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.watsi.uhp.R;

import java.util.ArrayList;

public class AddNewBillableFragment extends Fragment {

    private EditText nameField;
    private EditText priceField;
    private Button addBillableButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.add_new_billable_fragment_label);

        View view = inflater.inflate(R.layout.fragment_add_new_billable, container, false);

        nameField = (EditText) view.findViewById(R.id.name_field);
        priceField = (EditText) view.findViewById(R.id.price_field);
        addBillableButton = (Button) view.findViewById(R.id.add_billable_button);

        return view;
    }

    private void setAddBillableButton() {
        addBillableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ArrayList<String> newBillableArrayList = new ArrayList<String>();
                newBillableArrayList.add(nameField.getText().toString());
                newBillableArrayList.add(priceField.getText().toString());

                // TODO: below line should not go to new encounter fragment but previous one
                EncounterFragment encounterFragment = new EncounterFragment();
                Bundle bundle = new Bundle();
                bundle.putStringArrayList("newBillable", newBillableArrayList);
                encounterFragment.setArguments(bundle);

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, encounterFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}
