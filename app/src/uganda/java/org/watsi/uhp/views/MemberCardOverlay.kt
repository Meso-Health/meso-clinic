package org.watsi.uhp.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import org.watsi.uhp.R

class MemberCardOverlayView @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    companion object {
        const val CORNER_RADIUS = 45f
        const val HORIZONTAL_PADDING = 90f
        const val CARD_DIMENSION_RATIO = 52f / 79f
        const val OUTLINE_WIDTH = 12f
    }

    private val cornerRadiusArray = FloatArray(8, { CORNER_RADIUS })
    private val outlinePaint = Paint()
    private val translucentColor = getContext().getColor(R.color.translucentOverlay)
    private val maskPath = Path()
    private var outlinePath = Path()

    init {
        outlinePaint.color = Color.WHITE
        outlinePaint.strokeWidth = OUTLINE_WIDTH
        outlinePaint.style = Paint.Style.STROKE
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // clip the card shape from the surface and fill the outer area with translucent color
        maskPath.reset()
        val cy = canvas.height.toFloat() / 2
        val cardWidth = canvas.width - (2 * HORIZONTAL_PADDING)
        val cardHeight = cardWidth * CARD_DIMENSION_RATIO
        maskPath.addRoundRect(
                HORIZONTAL_PADDING,
                cy - (cardHeight / 2),
                canvas.width - HORIZONTAL_PADDING,
                canvas.height - cardHeight,
                cornerRadiusArray,
                Path.Direction.CW
        )

        maskPath.fillType = Path.FillType.INVERSE_EVEN_ODD
        canvas.clipPath(maskPath)
        canvas.drawColor(translucentColor)

        // draw the white outline of the card
        outlinePath.reset()
        outlinePath.addRoundRect(
                HORIZONTAL_PADDING,
                cy - (cardHeight / 2),
                canvas.width - HORIZONTAL_PADDING,
                canvas.height - cardHeight,
                cornerRadiusArray,
                Path.Direction.CW
        )

        canvas.drawPath(outlinePath, outlinePaint)
    }
}
