package org.watsi.uhp.helpers;

import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.Member;

/**
 * This class contains photo helper methods that would help with loading images smoothly using Glide.
 */

public class PhotoLoaderHelper {
    public static void loadMemberPhoto(Context context, Member member, ImageView photo, int placeholder, int width, int height) {
        if (FileManager.isLocal(member.getPhotoUrl())) {
            Glide.with(context)
                    .load(member.getPhotoUrl())
                    .override(getHeightFromDimensionResource(context, width), getWidthFromDimensionResource(context, height))
                    .centerCrop()
                    .placeholder(placeholder)
                    .into(photo);
        } else {
            // Need to clear because of https://github.com/bumptech/glide/issues/1275#issuecomment-226943312
            Glide.clear(photo);
            Bitmap photoBitmap = member.getPhotoBitmap(context.getContentResolver());
            if (photoBitmap != null) {
                photo.setImageBitmap(photoBitmap);
            } else {
                photo.setImageResource(R.drawable.portrait_placeholder);
            }
        }
    }

    protected static int getHeightFromDimensionResource(Context context, int height) {
        return (int) (context.getResources().getDimension(height) / context.getResources().getDisplayMetrics().density);
    }

    protected static int getWidthFromDimensionResource(Context context, int width) {
        return (int) (context.getResources().getDimension(width) / context.getResources().getDisplayMetrics().density);
    }
}
