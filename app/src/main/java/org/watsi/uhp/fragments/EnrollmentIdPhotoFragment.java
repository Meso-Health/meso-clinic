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

public class EnrollmentIdPhotoFragment extends EnrollmentFragment {

    static final int TAKE_ID_PHOTO_INTENT = 2;

    private ImageView mIdPhotoImageView;

    @Override
    int getTitleLabelId() {
        return R.string.enrollment_id_photo_fragment_label;
    }

    @Override
    int getFragmentLayoutId() {
        return R.layout.fragment_enrollment_id_photo;
    }

    @Override
    boolean isLastStep() {
        return false;
    }

    @Override
    void nextStep() {
        try {
            MemberDao.update(mMember);
            new NavigationManager(getActivity()).setEnrollmentContactInfoFragment(mMember.getId());
        } catch (SQLException e) {
            Rollbar.reportException(e);
            Toast.makeText(getContext(), "Failed to save photo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    void setUpFragment(View view) {
        Button capturePhotoBtn =
                (Button) view.findViewById(R.id.photo_btn);
        capturePhotoBtn.setOnClickListener(
                new CapturePhotoClickListener(TAKE_ID_PHOTO_INTENT, this));

        mIdPhotoImageView = (ImageView) view.findViewById(R.id.photo);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Bitmap photo = (Bitmap) data.getExtras().get("data");

        if (photo == null) {
            Rollbar.reportMessage("EnrollmentIdPhotoFragment failed to capture photo");
            Toast.makeText(getContext(), "Failed to capture photo", Toast.LENGTH_LONG).show();
            return;
        }

        mIdPhotoImageView.setImageBitmap(photo);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();
        mMember.setNationalIdPhoto(byteArray);

        try {
            stream.close();
        } catch (IOException e) {
            Rollbar.reportException(e);
        }
    }
}
