package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.ReceiptItemAdapter;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.managers.ConfigManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.EncounterItem;

import java.sql.SQLException;
import java.text.DecimalFormat;
import java.util.List;

public class ReceiptFragment extends Fragment {

    private Button mCreateEncounterButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.receipt_fragment_label);

        View view = inflater.inflate(R.layout.fragment_receipt, container, false);
        List<EncounterItem> encounterItems = ((MainActivity) getActivity()).getCurrentLineItems();
        mCreateEncounterButton = (Button) view.findViewById(R.id.create_encounter);

        ListView listView = (ListView) view.findViewById(R.id.receipt_items);
        Adapter mAdapter = new ReceiptItemAdapter(getActivity(), encounterItems);
        listView.setAdapter((ListAdapter) mAdapter);

        TextView priceTextView = (TextView) view.findViewById(R.id.total_price);

        DecimalFormat df = new DecimalFormat("#,###,###");
        priceTextView.setText(df.format(priceTotal(encounterItems)) + " UGX");

        setCreateEncounterButton();
        return view;
    }

    private int priceTotal(List<EncounterItem> encounterItems) {
        int sum = 0;
        for (EncounterItem item : encounterItems) {
            sum = sum + (item.getBillable().getPrice() * item.getQuantity());
        }
        return sum;
    }

    private void setCreateEncounterButton() {
        mCreateEncounterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MainActivity activity = (MainActivity) getActivity();

                try {
                    Encounter encounter = activity.getCurrentEncounter();
                    encounter.setToken(ConfigManager.getLoggedInUserToken(getContext()));
                    EncounterDao.create(encounter);
                } catch (SQLException e) {
                    Rollbar.reportException(e);
                }

                new NavigationManager(activity).setCurrentPatientsFragment();

                String toastMessage = activity.getCurrentEncounter()
                        .getMember()
                        .getFullName() + getString(R.string.encounter_submitted);

                Toast.makeText(
                        activity.getApplicationContext(),
                        toastMessage,
                        Toast.LENGTH_LONG
                ).show();
            }
        });
    }
}
