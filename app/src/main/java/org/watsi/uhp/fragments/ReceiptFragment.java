package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.LayoutInflaterCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

import org.watsi.uhp.R;
import org.watsi.uhp.adapters.ReceiptItemAdapter;

public class ReceiptFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        View view = inflater.inflate(R.layout.fragment_receipt, container, false);
//        ListView listView = (ListView) view.findViewById(R.id.receipt_items);
//        ListAdapter adapter = new ReceiptItemAdapter(getContext());
//        listView.setAdapter(adapter);


        return view;
    }
}
