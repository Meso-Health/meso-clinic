package org.watsi.uhp.helpers;

import android.content.Context;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

import org.watsi.uhp.R;
import org.watsi.uhp.managers.FileManager;
import org.watsi.uhp.models.Member;

/**
 * This class contains photo helper methods that would help with loading images smoothly using Glide.
 */

public class PhotoLoaderHelper {
    public static void loadMemberPhoto(Context context, Member member, ImageView imageView, int width, int height) {
        String fullSizePhotoUrl = member.getPhotoUrl();
        int adjustedWidth = getWidthFromDimensionResource(context, width);
        int adjustedHeight =  getHeightFromDimensionResource(context, height);
        if (fullSizePhotoUrl != null && FileManager.isLocal(fullSizePhotoUrl)) {
            loadFullSizeImageWithGlide(context, imageView, fullSizePhotoUrl, adjustedWidth, adjustedHeight);
        } else {
            // Reason we still with Glide for small images is that it is best practice to load
            // the same loading mechanism for list views according to this post on reddit:
            // https://www.reddit.com/r/androiddev/comments/3hlkbx/should_you_use_an_image_loading_lib_picasso_glide/cu8scpv/
            loadSmallPhotoWithGlide(context, imageView, member.getPhoto(), adjustedWidth, adjustedHeight);
        }
    }

    protected static void loadSmallPhotoWithGlide(Context context, ImageView imageView, byte[] photoBytes, int width, int height) {
        Glide.with(context)
                .load(photoBytes)
                .asBitmap()
                .override(width, height)
                .centerCrop()
                .placeholder(R.drawable.portrait_placeholder)
                .into(imageView);
    }

    protected static void loadFullSizeImageWithGlide(Context context, ImageView imageView, String fullSizePhotoUrl, int width, int height) {
        Glide.with(context)
                .load(fullSizePhotoUrl)
                .override(width, height)
                .centerCrop()
                .into(imageView);
    }

    protected static int getHeightFromDimensionResource(Context context, int height) {
        return (int) (context.getResources().getDimension(height) / context.getResources().getDisplayMetrics().density);
    }

    protected static int getWidthFromDimensionResource(Context context, int width) {
        return (int) (context.getResources().getDimension(width) / context.getResources().getDisplayMetrics().density);
    }
}
