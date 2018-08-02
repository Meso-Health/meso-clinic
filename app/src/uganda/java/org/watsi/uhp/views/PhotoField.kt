package org.watsi.uhp.views

import android.content.Context
import android.graphics.Bitmap
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.view_button_with_preview.view.button
import kotlinx.android.synthetic.uganda.view_photo_field.view.photo_button_with_preview
import kotlinx.android.synthetic.uganda.view_photo_field.view.photo_error_message
import org.watsi.uhp.R

class PhotoField @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_photo_field, this, true)
    }

    fun setPhotoPreview(thumbnailBitmap: Bitmap) {
        photo_button_with_preview.setPhotoPreview(thumbnailBitmap)
    }

    fun setError(errorMessage: String?) {
        photo_error_message.error = errorMessage
        button.toggleErrorState(errorMessage != null)
    }

    override fun setOnClickListener(listener: OnClickListener) {
        photo_button_with_preview.setOnClickListener(listener)
    }
}
