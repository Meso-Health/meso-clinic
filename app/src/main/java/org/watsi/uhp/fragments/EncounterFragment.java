package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import org.watsi.uhp.R;
import android.util.Log;

public class EncounterFragment extends Fragment {

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        String idMethod = getArguments().getString("idMethod");
        Log.d("UHP", "intention memberId: " + idMethod);
        return inflater.inflate(R.layout.fragment_encounter, container, false);
    }
}
