package org.watsi.uhp.views


import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.Drawable
import android.support.annotation.ColorInt
import android.support.annotation.DrawableRes
import android.support.v7.widget.AppCompatButton
import android.util.AttributeSet
import org.watsi.uhp.R

/**
 * Button View that centers an image and text
 *
 * Right now this only works for specifying a drawableLeft. Also includes helper functions for
 * updating the styling of the button to a "selected" state
 *
 * Ref: https://stackoverflow.com/questions/4817449/how-to-have-image-and-text-center-within-a-button
 */
class ImageTextButton @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : AppCompatButton(context, attrs, defStyleAttr) {

    private val drawableBounds = Rect()
    private val textBounds = Rect()
    private val imageTextSpace = resources.getDimensionPixelSize(R.dimen.space16)
    private val initialBackground = background
    private val initialTextColor = currentTextColor
    private val selectedBackground = resources.getDrawable(R.drawable.button_primary_background, null)
    private val errorBackground = resources.getDrawable(R.drawable.button_error_state_background, null)

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)

        if (!changed) return

        centerImage()
    }

    private fun centerImage() {
        paint.getTextBounds(text.toString(), 0, text.length, textBounds)

        val interiorWidth = width - paddingLeft - paddingRight

        // this assumes we want to center the leftDrawable
        // we would have to update this if we want to support different orientations
        val leftDrawable = compoundDrawables[0]
        leftDrawable.copyBounds(drawableBounds)
        val leftOffset = (interiorWidth - (textBounds.width() + drawableBounds.width()) +
                rightPaddingOffset) / 2 - compoundDrawablePadding - imageTextSpace
        drawableBounds.offset(leftOffset, 0)
        leftDrawable.bounds = drawableBounds
    }

    /**
     * Updates the button to a "selected" state
     *
     * The current "selected" state is the primary button styling with a check mark as the drawable
     */
    fun setSelected() {
        val selectedTextColor = resources.getColor(R.color.white, null)
        updateStyle(selectedBackground, selectedTextColor, R.drawable.ic_check_white_24dp)
    }

    /**
     * Removes the "selected" styling and returns the button to its original background and text
     */
    fun setUnselected(@DrawableRes drawableId: Int) {
        updateStyle(initialBackground, initialTextColor, drawableId)
    }

    private fun updateStyle(backgroundDrawable: Drawable,
                            @ColorInt textColorId: Int,
                            @DrawableRes drawableId: Int) {
        background = backgroundDrawable
        setTextColor(textColorId)
        setCompoundDrawablesWithIntrinsicBounds(drawableId, 0, 0, 0)
        centerImage()
    }

    fun toggleErrorState(error: Boolean) {
        if (error) {
            background = errorBackground
        }
    }
}
