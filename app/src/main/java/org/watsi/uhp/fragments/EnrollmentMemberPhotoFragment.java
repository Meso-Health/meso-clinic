package org.watsi.uhp.fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
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
import org.watsi.uhp.models.Member;

import java.io.IOException;
import java.sql.SQLException;

public class EnrollmentMemberPhotoFragment extends FormFragment<Member> {

    static final int TAKE_MEMBER_PHOTO_INTENT = 1;

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
        if (!mSyncableModel.shouldCaptureFingerprint()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setMessage(R.string.enrollment_fingerprint_confirm_completion);
            builder.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    try {
                        mSyncableModel.saveChanges(getAuthenticationToken());
                        getNavigationManager().setCurrentPatientsFragment();
                        confirmationToast().show();
                    } catch (SQLException e) {
                        ExceptionManager.reportException(e);
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
            getNavigationManager().setEnrollmentIdPhotoFragment(mSyncableModel);
        } else {
            getNavigationManager().setEnrollmentContactInfoFragment(mSyncableModel);
        }
    }

    private Toast confirmationToast() {
       if (mSyncableModel.isAbsentee()) {
           return Toast.makeText(getContext(), "Any updates successfully saved", Toast.LENGTH_LONG);
       } else {
           return Toast.makeText(getContext(), "Enrollment completed", Toast.LENGTH_LONG);
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
            } catch (IOException e) {
                ExceptionManager.reportException(e);
            }

            mSyncableModel.setPhotoUrl(mUri.toString());
            mSaveBtn.setEnabled(true);
        } else {
            Toast.makeText(getContext(), R.string.image_capture_failed, Toast.LENGTH_LONG).show();
        }
    }
}
