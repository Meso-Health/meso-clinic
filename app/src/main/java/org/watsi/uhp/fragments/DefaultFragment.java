package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.ReceptionActivity;

public class DefaultFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.default_fragment, container, false);

        Button scanBarcodeButton = (Button) view.findViewById(R.id.barcode_button);
        scanBarcodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ReceptionActivity activity = (ReceptionActivity) getActivity();
                activity.setBarcodeFragment();
            }
        });

        return view;
    }
}
