package org.watsi.uhp.helpers

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import org.watsi.uhp.R

/**
 * This class contains photo helper methods that would help with loading images smoothly using Glide.
 */
object PhotoLoader {
    fun loadMemberPhoto(bytes: ByteArray, view: ImageView, context: Context) {
        Glide.with(context)
                .load(bytes)
                .asBitmap()
                .centerCrop()
                .placeholder(R.drawable.portrait_placeholder)
                .into(view)
    }
}
