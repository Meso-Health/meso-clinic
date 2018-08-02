package org.watsi.uhp.views

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import org.watsi.uhp.R

/**
 * View class for displaying a circle indicator based on the last touch event
 *
 * This is used in the PhotoActivity to show the current region
 * where the exposure is being calculated from
 */
class TouchIndicator @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val indicatorPaint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var touchCoords: Pair<Float, Float>? = null

    init {
        indicatorPaint.color = context.getColor(R.color.translucentWhite)
        indicatorPaint.strokeWidth = 5f
        indicatorPaint.style = Paint.Style.STROKE
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {
        if (event?.action == MotionEvent.ACTION_DOWN) {
            touchCoords = Pair(event.x, event.y)
            postInvalidate()
        }
        // Must return false so that touch event gets passed to other touch-event listeners.
        // This is needed for the PhotoActivity because there is separate touch-event logic
        // for updating the exposure region on the camera preview surface.
        return false
    }

    override fun onDraw(canvas: Canvas?) {
        val lastTouchCoords = touchCoords
        if (lastTouchCoords != null) {
            canvas?.drawCircle(lastTouchCoords.first, lastTouchCoords.second, 100f, indicatorPaint)
        }
    }
}
