package org.watsi.uhp.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
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

import static org.watsi.uhp.R.string.save_btn_label;

public class EnrollNewbornPhotoFragment extends FormFragment<Member> {

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
    public boolean isFirstStep() {
        return false;
    }

    @Override
    public void nextStep() {
        try {
            mSyncableModel.saveChanges(getAuthenticationToken());
            IdentificationEvent idEvent = (IdentificationEvent) getArguments().getSerializable(NavigationManager.IDENTIFICATION_EVENT_BUNDLE_FIELD);
            getNavigationManager().setMemberDetailFragment(mSyncableModel, idEvent);
            Toast.makeText(getContext(), "Enrollment completed", Toast.LENGTH_LONG).show();
        } catch (SQLException | AbstractModel.ValidationException e) {
            ExceptionManager.reportException(e);
            Toast.makeText(getContext(), "Failed to save photo", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    void setUpFragment(View view) {
        try {
            String filename = "newborn_" + Clock.getCurrentTime().getTime() + ".jpg";
            mUri = FileManager.getUriFromProvider(filename, "member", getContext());
        } catch (IOException e) {
            ExceptionManager.reportException(e);
            getNavigationManager().setMemberDetailFragment(mSyncableModel);
            Toast.makeText(getContext(), R.string.generic_error_message, Toast.LENGTH_LONG).show();
        }

        Button capturePhotoBtn = ((Button) view.findViewById(R.id.photo_btn));
        Button savePhotoBtn = ((Button) view.findViewById(R.id.save_button));

        capturePhotoBtn.setText(R.string.enrollment_member_photo_btn);
        capturePhotoBtn.setOnClickListener(
                new CapturePhotoClickListener(TAKE_NEWBORN_PHOTO_INTENT, this, mUri));

        savePhotoBtn.setText(save_btn_label);

        mNewbornPhotoImageView = (ImageView) view.findViewById(R.id.photo);
        mSaveBtn.setEnabled(false);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == TAKE_NEWBORN_PHOTO_INTENT && resultCode == Activity.RESULT_OK) {

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mUri);
                mNewbornPhotoImageView.setImageBitmap(bitmap);
                mSyncableModel.setPhotoUrl(mUri.toString());
                mSaveBtn.setEnabled(true);
                return;
            } catch (IOException | AbstractModel.ValidationException e) {
                ExceptionManager.reportException(e);
            }
        }
        mSaveBtn.setEnabled(true);
        Toast.makeText(getContext(), R.string.image_capture_failed, Toast.LENGTH_LONG).show();
    }
}
