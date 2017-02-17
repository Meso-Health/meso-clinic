package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Adapter;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.ReceiptItemAdapter;
import org.watsi.uhp.models.LineItem;

import java.util.List;

import static android.R.id.list;

public class ReceiptFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_receipt, container, false);
        List<LineItem> mLineItems = getArguments().getParcelableArrayList("lineItems");

        ListView listView = (ListView) view.findViewById(R.id.receipt_items);
        Adapter mAdapter = new ReceiptItemAdapter(getActivity(), mLineItems);
        listView.setAdapter((ListAdapter) mAdapter);

        TextView priceTextView = (TextView) view.findViewById(R.id.total_price);
        priceTextView.setText(Integer.toString(priceTotal(mLineItems)));

        return view;
    }

    private int priceTotal(List<LineItem> lineItems) {
        int sum = 0;
        for (LineItem item : lineItems) {
            sum = sum + (item.getBillable().getPrice() * item.getQuantity());
        }
        return sum;
    }

}
