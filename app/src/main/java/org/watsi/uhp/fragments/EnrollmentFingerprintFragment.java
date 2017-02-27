package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;

public class EnrollmentFingerprintFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.enrollment_fingerprint_label);
        View view = inflater.inflate(R.layout.fragment_enrollment_fingerprint, container, false);

        Button continueBtn = (Button) view.findViewById(R.id.enrollment_fingerprint_save_btn);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setCurrentPatientsFragment();
            }
        });
        return view;
    }
}
