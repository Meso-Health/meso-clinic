package org.watsi.uhp.listeners;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;

public class CapturePhotoClickListener implements View.OnClickListener {

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
