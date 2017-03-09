package org.watsi.uhp.listeners;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.View;

public class CapturePhotoClickListener implements View.OnClickListener {

    private int mRequestCode;
    private Fragment mFragment;
    private Uri mUri;

    public CapturePhotoClickListener(int intentRequestCode, Fragment fragment, Uri uri) {
        this.mRequestCode = intentRequestCode;
        this.mFragment = fragment;
        this.mUri = uri;
    }

    @Override
    public void onClick(View v) {
        Intent takeMemberPhotoIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        takeMemberPhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, mUri);
        PackageManager packageManager = mFragment.getActivity().getPackageManager();
        if (takeMemberPhotoIntent.resolveActivity(packageManager) != null) {
            mFragment.startActivityForResult(takeMemberPhotoIntent, mRequestCode);
        }
    }
}
