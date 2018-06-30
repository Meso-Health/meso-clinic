package org.watsi.uhp.helpers

import android.content.Context
import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import org.watsi.domain.entities.Member
import org.watsi.uhp.R

/**
 * This class contains photo helper methods that would help with loading images smoothly using Glide.
 */
object PhotoLoader {

    const val ROUNDING_RADIUS = 8
    val requestOptions = RequestOptions().transforms(CenterCrop(), RoundedCorners(ROUNDING_RADIUS))

    fun loadPhoto(bytes: ByteArray?, view: ImageView, context: Context) {
        Glide.with(context)
                .load(bytes)
                .apply(requestOptions)
                .into(view)
    }

    fun loadMemberPhoto(bytes: ByteArray?, view: ImageView, context: Context, gender: Member.Gender) {
        val placeholder = if (gender == Member.Gender.F) {
            R.drawable.ic_member_placeholder_female
        } else {
            R.drawable.ic_member_placeholder_male
        }

        requestOptions.fallback(placeholder)

        Glide.with(context)
                .load(bytes)
                .apply(requestOptions)
                .into(view)
    }
}
