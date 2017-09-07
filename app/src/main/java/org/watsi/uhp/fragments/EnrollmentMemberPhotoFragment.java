package org.watsi.uhp.fragments;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import org.watsi.uhp.presenters.EnrollmentPresenter;

import java.io.IOException;
import java.sql.SQLException;

public class EnrollmentMemberPhotoFragment extends PhotoFragment<Member> {

    private IdentificationEvent mIdEvent;
    private EnrollmentPresenter enrollmentPresenter;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_member_photo_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_capture_photo;
    }

    @Override
    public boolean isFirstStep() {
        return true;
    }

    @Override
    public void nextStep() {
        if (!mSyncableModel.shouldCaptureFingerprint()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.enrollment_fingerprint_confirm_completion);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        mSyncableModel.saveChanges(getAuthenticationToken());
                        getNavigationManager().setMemberDetailFragment(mSyncableModel, mIdEvent);
                        enrollmentPresenter.confirmationToast().show();
                    } catch (SQLException | AbstractModel.ValidationException e) {
                        ExceptionManager.reportException(e, "Tried to save changes to a member that has invalid fields for member id: " + mSyncableModel.getId());
                        Toast.makeText(getContext(), "Failed to save photo", Toast.LENGTH_LONG).show();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        } else if (mSyncableModel.shouldCaptureNationalIdPhoto()) {
            getNavigationManager().setEnrollmentIdPhotoFragment(mSyncableModel, mIdEvent);
        } else {
            getNavigationManager().setEnrollmentContactInfoFragment(mSyncableModel, mIdEvent);
        }
    }

    @Override
    void handleSetupFailure() {
        getNavigationManager().setCurrentPatientsFragment();
        Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
    }

    @Override
    void additionalSetup(View view) {
        ((Button) view.findViewById(R.id.photo_btn)).setText(R.string.enrollment_member_photo_btn);
        mIdEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);

        if (!mSyncableModel.shouldCaptureFingerprint()) {
            mSaveBtn.setText(R.string.enrollment_complete_btn);
        }

        enrollmentPresenter = new EnrollmentPresenter(mSyncableModel, getContext());
    }

    @Override
    void onPhotoCaptured(Photo photo) throws IOException {
        mSyncableModel.setLocalMemberPhoto(photo);
        mSaveBtn.setEnabled(true);
    }
}
