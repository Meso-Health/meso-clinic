package org.watsi.uhp.helpers

import android.content.Context
import android.widget.ImageView

import com.bumptech.glide.Glide

import org.watsi.domain.entities.Member
import org.watsi.domain.repositories.PhotoRepository
import org.watsi.uhp.R

/**
 * This class contains photo helper methods that would help with loading images smoothly using Glide.
 */
class PhotoLoaderHelper(private val context: Context, private val photoRepository: PhotoRepository) {
    // It's OK for the thumbnail sized photos to be a little higher resolution.
    private val THUMBNAIL_SIZE_MULTIPLIER = 2

    fun loadMemberPhoto(member: Member, imageView: ImageView, width: Int, height: Int) {
        val adjustedWidth = getWidthFromDimensionResource(context, width)
        val adjustedHeight = getHeightFromDimensionResource(context, height)
        // TODO: fix when we fix photo handling logic
        if (member.thumbnailPhotoId != null) {
            member.thumbnailPhotoId?.let { photoId ->
//                val photo = photoRepository.find(photoId)
//                loadPhotoFromBytes(imageView, photo.bytes, adjustedWidth, adjustedHeight)
            }
        } else if (member.photoId != null) {
            member.photoId?.let { photoId ->
//                photoRepository.find(photoId).url?.let { photoUrl ->
//                    loadPhotoFromContentUri(imageView, photoUrl, adjustedWidth, adjustedHeight)
//                }
            }
        }
    }

    private fun loadPhotoFromBytes(imageView: ImageView,
                                   photoBytes: ByteArray?,
                                   width: Int,
                                   height: Int) {
        Glide.with(context)
                .load(photoBytes)
                .asBitmap()
                .override(width * THUMBNAIL_SIZE_MULTIPLIER, height * THUMBNAIL_SIZE_MULTIPLIER)
                .centerCrop()
                .placeholder(R.drawable.portrait_placeholder)
                .into(imageView)
    }

    private fun loadPhotoFromContentUri(imageView: ImageView,
                                        fullSizePhotoUrl: String,
                                        width: Int,
                                        height: Int) {
        Glide.with(context)
                .load(fullSizePhotoUrl)
                .override(width, height)
                .centerCrop()
                .into(imageView)
    }

    private fun getHeightFromDimensionResource(context: Context, height: Int): Int {
        return (context.resources.getDimension(height) / context.resources.displayMetrics.density).toInt()
    }

    private fun getWidthFromDimensionResource(context: Context, width: Int): Int {
        return (context.resources.getDimension(width) / context.resources.displayMetrics.density).toInt()
    }
}
