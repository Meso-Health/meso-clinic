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

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;
import org.watsi.uhp.adapters.ReceiptItemAdapter;
import org.watsi.uhp.database.EncounterDao;
import org.watsi.uhp.database.LineItemDao;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.models.Encounter;
import org.watsi.uhp.models.LineItem;

import java.sql.SQLException;
import java.util.Calendar;
import java.util.List;

public class ReceiptFragment extends Fragment {

    private List<LineItem> mLineItems;
    private Button mCreateEncounterButton;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_receipt, container, false);
        mLineItems = getArguments().getParcelableArrayList("lineItems");
        mCreateEncounterButton = (Button) view.findViewById(R.id.create_encounter);

        ListView listView = (ListView) view.findViewById(R.id.receipt_items);
        Adapter mAdapter = new ReceiptItemAdapter(getActivity(), mLineItems);
        listView.setAdapter((ListAdapter) mAdapter);

        TextView priceTextView = (TextView) view.findViewById(R.id.total_price);
        priceTextView.setText(Integer.toString(priceTotal(mLineItems)) + " UGX");

        setCreateEncounterButton();
        return view;
    }

    private int priceTotal(List<LineItem> lineItems) {
        int sum = 0;
        for (LineItem item : lineItems) {
            sum = sum + (item.getBillable().getPrice() * item.getQuantity());
        }
        return sum;
    }

    private void setCreateEncounterButton() {
        mCreateEncounterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO: this should be in a transaction
                Encounter encounter = new Encounter();
                encounter.setDate(Calendar.getInstance().getTime());
                try {
                    // TODO: get actual member instead of arbitrarily selecting first
                    encounter.setMember(MemberDao.all().get(0));
                    EncounterDao.create(encounter);
                    LineItemDao.create(mLineItems);
                } catch (SQLException e) {
                    Rollbar.reportException(e);
                }

                // TODO: clear backstack
                ((MainActivity) getActivity()).setMainFragment();
            }
        });
    }
}
