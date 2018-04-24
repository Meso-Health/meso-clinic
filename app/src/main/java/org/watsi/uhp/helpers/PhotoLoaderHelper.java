package org.watsi.uhp.helpers;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.watsi.domain.entities.Member;
import org.watsi.uhp.R;

/**
 * This class contains photo helper methods that would help with loading images smoothly using Glide.
 */

public class PhotoLoaderHelper {
    // It's OK for the thumbnail sized photos to be a little higher resolution.
    private static int THUMBNAIL_SIZE_MULTIPLIER = 2;

    public static void loadMemberPhoto(Context context, Member member, ImageView imageView, int width, int height) {
        int adjustedWidth = getWidthFromDimensionResource(context, width);
        int adjustedHeight = getHeightFromDimensionResource(context, height);
        if (member.getThumbnailPhotoId() != null) {
            // TODO: load photo from thumbnail
//            loadPhotoFromBytes(context, imageView, member.getCroppedPhotoBytes(), adjustedWidth, adjustedHeight);
        } else {
            if (member.getPhotoId() != null) {
                // TODO: load photo from URL
//                loadPhotoFromContentUri(context, imageView, member.getLocalMemberPhoto().getUrl(), adjustedWidth, adjustedHeight);
            }
        }
    }

    static void loadPhotoFromBytes(Context context, ImageView imageView, byte[] photoBytes, int width, int height) {
        Glide.with(context)
                .load(photoBytes)
                .asBitmap()
                .override(width * THUMBNAIL_SIZE_MULTIPLIER, height * THUMBNAIL_SIZE_MULTIPLIER)
                .centerCrop()
                .placeholder(R.drawable.portrait_placeholder)
                .into(imageView);
    }

    static void loadPhotoFromContentUri(Context context, ImageView imageView, String fullSizePhotoUrl, int width, int height) {
        Glide.with(context)
                .load(fullSizePhotoUrl)
                .override(width, height)
                .centerCrop()
                .into(imageView);
    }

    static int getHeightFromDimensionResource(Context context, int height) {
        return (int) (context.getResources().getDimension(height) / context.getResources().getDisplayMetrics().density);
    }

    static int getWidthFromDimensionResource(Context context, int width) {
        return (int) (context.getResources().getDimension(width) / context.getResources().getDisplayMetrics().density);
    }
}
