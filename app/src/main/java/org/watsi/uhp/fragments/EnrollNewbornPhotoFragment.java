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

import java.io.IOException;
import java.sql.SQLException;

public class EnrollNewbornPhotoFragment extends EnrollmentFragment {

    static final int TAKE_NEWBORN_PHOTO_INTENT = 4;

    private ImageView mNewbornPhotoImageView;
    private Uri mUri;

    @Override
    int getTitleLabelId() {
        return R.string.enroll_newborn_photo_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_capture_photo;
    }

    @Override
    boolean isLastStep() {
        return true;
    }

    @Override
    void nextStep() {
        try {
            mMember.saveChanges(getAuthenticationToken());
            getNavigationManager().setCurrentPatientsFragment();
            Toast.makeText(getContext(), "Enrollment completed", Toast.LENGTH_LONG).show();
        } catch (SQLException e) {
            ExceptionManager.reportException(e);
            Toast.makeText(getContext(), "Failed to save photo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    void setUpFragment(View view) {
        ((Button) view.findViewById(R.id.photo_btn)).setText(R.string.enrollment_member_photo_btn);
        try {
            String filename = "newborn_" + Clock.getCurrentTime().getTime() + ".jpg";
            mUri = FileManager.getUriFromProvider(filename, "member", getContext());
        } catch (IOException e) {
            ExceptionManager.reportException(e);
            getNavigationManager().setCurrentPatientsFragment();
            Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
        }

        Button capturePhotoBtn =
                (Button) view.findViewById(R.id.photo_btn);
        capturePhotoBtn.setOnClickListener(
                new CapturePhotoClickListener(TAKE_NEWBORN_PHOTO_INTENT, this, mUri));

        mNewbornPhotoImageView = (ImageView) view.findViewById(R.id.photo);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_NEWBORN_PHOTO_INTENT && resultCode == Activity.RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mUri);
                mNewbornPhotoImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                ExceptionManager.reportException(e);
            }

            mMember.setPhotoUrl(mUri.toString());
            mSaveBtn.setEnabled(true);
        } else {
            Toast.makeText(getContext(), R.string.image_capture_failed, Toast.LENGTH_LONG).show();
        }
    }
}
