package org.watsi.uhp.views

import android.content.Context
import android.graphics.Bitmap
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_button_with_preview.view.button
import kotlinx.android.synthetic.main.view_button_with_preview.view.id_preview
import kotlinx.android.synthetic.main.view_button_with_preview.view.photo_preview
import kotlinx.android.synthetic.main.view_button_with_preview.view.preview_container
import kotlinx.android.synthetic.main.view_button_with_preview.view.preview_icon
import kotlinx.android.synthetic.main.view_button_with_preview.view.success_message_preview
import org.watsi.uhp.R

class ButtonWithPreview @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_button_with_preview, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.ButtonWithContainer)

        button.text = customAttributes.getString(R.styleable.ButtonWithContainer_buttonText)
        button.setCompoundDrawablesWithIntrinsicBounds(customAttributes.getDrawable(R.styleable.ButtonWithContainer_icon), null, null, null)
        // must grab icon again separately; if we share the variable then changing the tint will affect both
        preview_icon.setImageDrawable(customAttributes.getDrawable(R.styleable.ButtonWithContainer_icon))
        preview_icon.imageTintList = context.getColorStateList(R.color.gray6)

        customAttributes.recycle()
    }

    override fun setOnClickListener(listener: OnClickListener) {
        listOf(button, preview_container).forEach { view ->
            view.setOnClickListener {
                this.requestFocus()
                listener.onClick(view)
            }
        }
    }

    fun enableButton() {
        button.isClickable = true
        button.alpha = 1.0f
    }

    fun disableButton() {
        button.isClickable = false
        button.alpha = 0.3f
    }

    private fun togglePreviewOn() {
        button.visibility = View.GONE
        preview_container.visibility = View.VISIBLE
    }

    fun setIdPreview(id: String) {
        togglePreviewOn()
        id_preview.text = id
        id_preview.visibility = View.VISIBLE
    }

    fun setPhotoPreview(bitmap: Bitmap) {
        togglePreviewOn()
        photo_preview.setImageBitmap(bitmap)
        photo_preview.visibility = View.VISIBLE
    }

    fun setSuccessMessagePreview(messageStringId: Int) {
        togglePreviewOn()
        success_message_preview.setText(messageStringId)
        success_message_preview.visibility = View.VISIBLE
    }
}
