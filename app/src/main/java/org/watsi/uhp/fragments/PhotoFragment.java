package org.watsi.uhp.fragments;

import android.app.Activity;
import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import org.watsi.uhp.R;
import org.watsi.uhp.listeners.CapturePhotoClickListener;
import org.watsi.uhp.managers.Clock;
import org.watsi.uhp.managers.ExceptionManager;
import org.watsi.uhp.models.Photo;
import org.watsi.uhp.models.SyncableModel;

import java.io.IOException;
import java.sql.SQLException;

public abstract class PhotoFragment<T extends SyncableModel> extends FormFragment<T> {

    private static int CAPTURE_PHOTO_INTENT = 111;
    private static int DELETE_GALLERY_PHOTO_INTERVAL_IN_MS = 60000;

    private Uri mUri;

    abstract void handleSetupFailure();
    abstract void additionalSetup(View view);
    abstract void onPhotoCaptured(Photo photo) throws IOException;

    @Override
    void setUpFragment(View view) {
        try {
            String filename = Clock.getCurrentTime().getTime() + ".jpg";
            mUri = Photo.getUriFromProvider(filename, getContext());
        } catch (IOException e) {
            ExceptionManager.reportException(e);
            handleSetupFailure();
        }

        view.findViewById(R.id.photo_btn).setOnClickListener(
                new CapturePhotoClickListener(CAPTURE_PHOTO_INTENT, this, mUri));

        additionalSetup(view);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        try {
            if (requestCode == CAPTURE_PHOTO_INTENT && resultCode == Activity.RESULT_OK) {
                deleteMediaStoreDuplicate();

                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), mUri);
                View view = getView();
                if (view != null) ((ImageView) view.findViewById(R.id.photo)).setImageBitmap(bitmap);

                Photo photo = new Photo();
                photo.setUrl(mUri.toString());
                photo.create();

                onPhotoCaptured(photo);
            } else {
                ExceptionManager.reportErrorMessage("Image capture intent failed");
                Toast.makeText(getContext(), R.string.image_capture_failed, Toast.LENGTH_LONG).show();
            }

        } catch (IOException | SQLException e) {
            ExceptionManager.reportException(e);
            Toast.makeText(getContext(), R.string.image_failed_to_save, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * This method deletes the duplicate photo that is stored in the phone's public gallery
     * as a result of using an ACTION_IMAGE_CAPTURE intent to capture the photo by querying
     * for the last taken photo and deleting it if it was created within the last minute
     */
    protected void deleteMediaStoreDuplicate() {
        Uri mediaStoreImageUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = new String[] {
                MediaStore.Images.Media._ID,
                MediaStore.Images.Media.DATE_TAKEN
        };

        Long oneMinuteAgo = Clock.getCurrentTime().getTime() - DELETE_GALLERY_PHOTO_INTERVAL_IN_MS;

        String whereClause = MediaStore.Images.Media.DATE_TAKEN + " > ?";
        String[] whereArgs = new String[]{ oneMinuteAgo.toString() };

        Cursor cursor = getContext().getContentResolver().query(
                mediaStoreImageUri, projection, whereClause, whereArgs,
                MediaStore.Images.Media.DATE_TAKEN + " DESC" );

        if (cursor == null) {
            ExceptionManager.reportErrorMessage("Null cursor returned when attempting to delete photo duplicate");
            return;
        }

        int cursorCount = cursor.getCount();
        if (cursorCount != 1) {
            ExceptionManager.reportErrorMessage(
                    "Unexpected number of photos returned from delete photo from gallery query: " + cursorCount);
        }

        while (cursor.moveToNext()) {
            long photoId = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID));
            Uri duplicatePhotoUri = ContentUris.withAppendedId(mediaStoreImageUri, photoId);
            getContext().getContentResolver().delete(duplicatePhotoUri, null, null);
        }

        cursor.close();
    }
}
