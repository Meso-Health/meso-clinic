package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;

import org.watsi.uhp.R;
import org.watsi.uhp.activities.MainActivity;

public class MainFragment extends Fragment {

    private Button mIdentificationButton;
    private Button mEncounterButton;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.main_activity_label);
        final LinearLayout view = (LinearLayout) inflater.inflate(R.layout.fragment_main, container, false);

        mIdentificationButton = (Button) view.findViewById(R.id.identification_button);
        mEncounterButton = (Button) view.findViewById(R.id.encounter_button);

        setButtons();
        return view;
    }

    private void setButtons() {
        mIdentificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setBarcodeFragment();
            }
        });

        mEncounterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((MainActivity) getActivity()).setCurrentPatientsFragment();
            }
        });
    }
}
