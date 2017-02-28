package org.watsi.uhp.fragments;

import android.content.Intent;
import android.graphics.Bitmap;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.listeners.CapturePhotoClickListener;
import org.watsi.uhp.managers.NavigationManager;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;

public class EnrollmentMemberPhotoFragment extends EnrollmentFragment {

    static final int TAKE_MEMBER_PHOTO_INTENT = 1;

    private ImageView mMemberPhotoImageView;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_member_photo_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_enrollment_member_photo;
    }

    @Override
    boolean isLastStep() {
        return !mMember.shouldCaptureFingerprint();
    }

    @Override
    void nextStep() {
        NavigationManager navigationManager = new NavigationManager(getActivity());
        if (!mMember.shouldCaptureFingerprint()) {
            mMember.setSynced(false);
        }

        try {
            MemberDao.update(mMember);
            if (!mMember.shouldCaptureFingerprint()) {
                navigationManager.setCurrentPatientsFragment();
                Toast.makeText(getContext(), "Enrolled!", Toast.LENGTH_LONG).show();
            } else if (mMember.shouldCaptureNationalIdPhoto()) {
                navigationManager.setEnrollmentIdPhotoFragment(mMember.getId());
            } else {
                navigationManager.setEnrollmentContactInfoFragment(mMember.getId());
            }
        } catch (SQLException e) {
            Rollbar.reportException(e);
            Toast.makeText(getContext(), "Failed to save photo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    void setUpFragment(View view) {
        Button capturePhotoBtn =
                (Button) view.findViewById(R.id.enrollment_photos_member_photo_button);
        capturePhotoBtn.setOnClickListener(
                new CapturePhotoClickListener(TAKE_MEMBER_PHOTO_INTENT, this));

        mMemberPhotoImageView = (ImageView) view.findViewById(R.id.photo);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap photo = (Bitmap) data.getExtras().get("data");

        if (photo == null) {
            Rollbar.reportMessage("EnrollmentMemberPhotoFragment failed to capture photo");
            Toast.makeText(getContext(), "Failed to capture photo", Toast.LENGTH_LONG).show();
            return;
        }

        mMemberPhotoImageView.setImageBitmap(photo);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        mMember.setPhoto(byteArray);
        mSaveBtn.setEnabled(true);

        try {
            stream.close();
        } catch (IOException e) {
            Rollbar.reportException(e);
        }
    }
}
