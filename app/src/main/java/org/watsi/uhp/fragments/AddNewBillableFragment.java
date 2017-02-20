package org.watsi.uhp.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.LineItemInterface;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Billable;
import org.watsi.uhp.models.LineItem;
import org.watsi.uhp.models.Member;

import java.util.List;

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

        setAddBillableButton();

        return view;
    }

    private void setAddBillableButton() {
        addBillableButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Billable billable = new Billable();
                billable.setName(nameField.getText().toString());
                billable.setPrice(Integer.parseInt(priceField.getText().toString()));
                billable.setCategory(Billable.CategoryEnum.UNSPECIFIED);

                LineItem lineItem = new LineItem();
                lineItem.setBillable(billable);

                Activity activity = getActivity();
                ((LineItemInterface) activity).addLineItem(lineItem);

                EncounterFragment encounterFragment = new EncounterFragment();

                FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
                transaction.replace(R.id.fragment_container, encounterFragment);
                transaction.addToBackStack(null);
                transaction.commit();
            }
        });
    }
}
