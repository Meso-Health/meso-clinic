package org.watsi.uhp.fragments;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.rollbar.android.Rollbar;

import org.watsi.uhp.R;
import org.watsi.uhp.database.MemberDao;
import org.watsi.uhp.managers.NavigationManager;
import org.watsi.uhp.models.Member;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.UUID;

public class EnrollmentPhotosFragment extends Fragment {

    static final int TAKE_MEMBER_PHOTO_INTENT = 1;
    static final int TAKE_ID_PHOTO_INTENT = 2;

    private ImageView mMemberPhotoImageView;
    private ImageView mIdPhotoImageView;
    private Member mMember;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(R.string.enrollment_photos_label);
        try {
            mMember = MemberDao.findById(UUID.fromString(getArguments().getString("memberId")));
        } catch (SQLException e) {
            Rollbar.reportException(e);
        }

        View view = inflater.inflate(R.layout.fragment_enrollment_photos, container, false);

        Button continueBtn = (Button) view.findViewById(R.id.enrollment_photos_save_button);
        continueBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new NavigationManager(getActivity())
                        .setEnrollmentFingerprintFragment(mMember.getId());
            }
        });

        Button captureMemberPhotoBtn =
                (Button) view.findViewById(R.id.enrollment_photos_member_photo_button);
        captureMemberPhotoBtn.setOnClickListener(
                new CapturePhotoClickListener(TAKE_MEMBER_PHOTO_INTENT, this));

        Button captureIdPhotoBtn =
                (Button) view.findViewById(R.id.enrollment_photos_id_photo_button);
        captureIdPhotoBtn.setOnClickListener(
                new CapturePhotoClickListener(TAKE_ID_PHOTO_INTENT, this));

        mMemberPhotoImageView = (ImageView) view.findViewById(R.id.member_photo);
        mIdPhotoImageView = (ImageView) view.findViewById(R.id.id_photo);

        return view;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        ImageView imageView = (requestCode == TAKE_MEMBER_PHOTO_INTENT) ?
                mMemberPhotoImageView : mIdPhotoImageView;

        Bitmap photo = (Bitmap) data.getExtras().get("data");
        imageView.setImageBitmap(photo);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        photo.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        byte[] byteArray = stream.toByteArray();

        if (requestCode == TAKE_MEMBER_PHOTO_INTENT) {
            mMember.setPhoto(byteArray);
        } else {
            mMember.setNationalIdPhoto(byteArray);
        }
        try {
            stream.close();
        } catch (IOException e) {
            Rollbar.reportException(e);
        }
    }

    private static class CapturePhotoClickListener implements View.OnClickListener {

        private int mRequestCode;
        private Fragment mFragment;

        public CapturePhotoClickListener(int intentRequestCode, Fragment fragment) {
            this.mRequestCode = intentRequestCode;
            this.mFragment = fragment;
        }

        @Override
        public void onClick(View v) {
            Intent takeMemberPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            PackageManager packageManager = mFragment.getActivity().getPackageManager();
            if (takeMemberPhotoIntent.resolveActivity(packageManager) != null) {
                mFragment.startActivityForResult(takeMemberPhotoIntent, mRequestCode);
            }
        }
    }
}
