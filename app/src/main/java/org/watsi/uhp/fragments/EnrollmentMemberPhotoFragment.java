package org.watsi.uhp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.listeners.CapturePhotoClickListener;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.AbstractModel;
import org.watsi.uhp.models.IdentificationEvent;
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;

public class EnrollmentMemberPhotoFragment extends FormFragment<Member> {

    static final int TAKE_MEMBER_PHOTO_INTENT = 1;

    private IdentificationEvent mIdEvent;
    private ImageView mMemberPhotoImageView;
    private Uri mUri;

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
    void nextStep(View view) {
        try {
            if (!mSyncableModel.shouldCaptureFingerprint()) {
                mSyncableModel.saveChanges(getAuthenticationToken());
                getNavigationManager().setMemberDetailFragment(mSyncableModel, mIdEvent);
                Toast.makeText(getContext(), "Enrollment completed", Toast.LENGTH_LONG).show();
            } else if (mSyncableModel.shouldCaptureNationalIdPhoto()) {
                getNavigationManager().setEnrollmentIdPhotoFragment(mSyncableModel, mIdEvent);
            } else {
                getNavigationManager().setEnrollmentContactInfoFragment(mSyncableModel, mIdEvent);
            }
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
            Toast.makeText(getContext(), "Failed to save photo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    void setUpFragment(View view) {
        ((Button) view.findViewById(R.id.photo_btn)).setText(R.string.enrollment_member_photo_btn);
        try {
            String filename = "member_" + mSyncableModel.getId().toString() +
                    "_" + Clock.getCurrentTime().getTime() + ".jpg";
            mUri = FileManager.getUriFromProvider(filename, "member", getContext());
        } catch (IOException e) {
            ExceptionManager.reportException(e);
            getNavigationManager().setCurrentPatientsFragment();
            Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
        }

        Button capturePhotoBtn =
                (Button) view.findViewById(R.id.photo_btn);
        capturePhotoBtn.setOnClickListener(
                new CapturePhotoClickListener(TAKE_MEMBER_PHOTO_INTENT, this, mUri));

        mMemberPhotoImageView = (ImageView) view.findViewById(R.id.photo);
        mIdEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);

        if (!mSyncableModel.shouldCaptureFingerprint()) {
            mSaveBtn.setText(R.string.enrollment_complete_btn);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_MEMBER_PHOTO_INTENT && resultCode == Activity.RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mUri);
                mMemberPhotoImageView.setImageBitmap(bitmap);
                mSyncableModel.setPhotoUrl(mUri.toString());
            } catch (IOException | AbstractModel.ValidationException e) {
                ExceptionManager.reportException(e);
            }

            mSaveBtn.setEnabled(true);
        } else {
            Toast.makeText(getContext(), R.string.image_capture_failed, Toast.LENGTH_LONG).show();
        }
    }
}
