package org.watsi.uhp.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;

public class EnrollmentPhotosFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle bundle) {
        getActivity().setTitle(R.string.enrollment_photos_label);
        View view = inflater.inflate(R.layout.fragment_enrollment_photos, container, false);

        Button continueBtn = (Button) view.findViewById(R.id.enrollment_photos_save_button);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity()).setEnrollmentFingerprintFragment();
            }
        });
        return view;
    }
}
