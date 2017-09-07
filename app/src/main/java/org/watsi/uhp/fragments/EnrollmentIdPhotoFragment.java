package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.Photo;

import java.io.IOException;

public class EnrollmentIdPhotoFragment extends PhotoFragment<Member> {

    private IdentificationEvent mIdEvent;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_id_photo_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_capture_photo;
    }

    @Override
    public boolean isFirstStep() {
        return false;
    }

    @Override
    public void nextStep() {
        getNavigationManager().setEnrollmentContactInfoFragment(mSyncableModel, mIdEvent);
    }

    @Override
    void handleSetupFailure() {
        getNavigationManager().setCurrentPatientsFragment();
        Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
    }

    @Override
    void additionalSetup(View view) {
        ((Button) view.findViewById(R.id.photo_btn)).setText(R.string.enrollment_id_photo_btn);

        mIdEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
    }

    @Override
    void onPhotoCaptured(Photo photo) throws IOException {
        mSyncableModel.setLocalNationalIdPhoto(photo);
        mSaveBtn.setEnabled(true);
    }
}
