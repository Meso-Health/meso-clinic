package org.watsi.uhp.fragments;

import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;
import org.watsi.uhp.models.Photo;

import java.io.IOException;
import java.sql.SQLException;

public class EnrollNewbornPhotoFragment extends PhotoFragment<Member> {

    @Override
    int getTitleLabelId() {
        return R.string.enroll_newborn_photo_label;
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
        try {
            mSyncableModel.saveChanges(getAuthenticationToken());
            IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
            Toast.makeText(getContext(), "Enrollment completed", Toast.LENGTH_LONG).show();
            getNavigationManager().setCheckInMemberDetailFragmentAfterEnrollNewborn(mSyncableModel, idEvent);
        } catch (SQLException | AbstractModel.ValidationException e) {
            Toast.makeText(getContext(), "Failed to save photo", Toast.LENGTH_LONG).show();
            ExceptionManager.reportException(e, "Failed to save changes to a member that has invalid fields for member id: " +  mSyncableModel.getId());
        }
    }

    @Override
    void handleSetupFailure() {
        getNavigationManager().setMemberDetailFragment(mSyncableModel);
        Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
    }

    @Override
    void additionalSetup(View view) {
        Button savePhotoBtn = ((Button) view.findViewById(R.id.save_button));
        savePhotoBtn.setText(R.string.save_btn_label);

        mSaveBtn.setEnabled(false);
    }

    @Override
    void onPhotoCaptured(Photo photo) throws IOException {
        mSyncableModel.setLocalMemberPhoto(photo);
        mSaveBtn.setEnabled(true);
    }
}
