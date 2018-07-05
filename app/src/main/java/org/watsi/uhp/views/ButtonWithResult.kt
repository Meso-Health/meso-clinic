package org.watsi.uhp.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import kotlinx.android.synthetic.main.view_button_with_result.view.button
import kotlinx.android.synthetic.main.view_button_with_result.view.result_container
import kotlinx.android.synthetic.main.view_button_with_result.view.success_icon
import kotlinx.android.synthetic.main.view_button_with_result.view.result_message
import org.watsi.uhp.R

class ButtonWithResult @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    companion object {
        val enabledTransparency = 1.0F
        val disabledTransparency = 0.3F
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_button_with_result, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.ButtonWithContainer)

        button.text = customAttributes.getString(R.styleable.ButtonWithContainer_buttonText)
        button.setCompoundDrawablesWithIntrinsicBounds(customAttributes.getDrawable(R.styleable.ButtonWithContainer_icon), null, null, null)

        customAttributes.recycle()
    }

    override fun setOnClickListener(listener: OnClickListener) {
        listOf(button, result_container).forEach { view ->
            view.setOnClickListener {
                this.requestFocus()
                listener.onClick(view)
            }
        }
    }

    fun enableButton() {
        button.visibility = View.VISIBLE
        result_container.visibility = View.GONE

        button.isClickable = true
        button.alpha = enabledTransparency
    }

    fun disableButton() {
        button.isClickable = false
        button.alpha = disabledTransparency
    }

    fun disableButtonWithClickListener(listener: OnClickListener) {
        button.alpha = disabledTransparency

        listOf(button, result_container).forEach { view ->
            view.setOnClickListener {
                this.requestFocus()
                listener.onClick(view)
            }
        }
    }

    fun showSuccess(message: String) {
        button.visibility = View.GONE
        result_container.visibility = View.VISIBLE

        result_container.setBackgroundResource(R.drawable.button_result_success)
        success_icon.visibility = View.VISIBLE
        result_message.text = message
        result_message.setTextColor(context.getColor(R.color.green9))
    }

    fun showFailure(message: String) {
        button.visibility = View.GONE
        result_container.visibility = View.VISIBLE

        result_container.setBackgroundResource(R.drawable.button_result_failure)
        success_icon.visibility = View.GONE
        result_message.text = message
        result_message.setTextColor(context.getColor(R.color.red9))
    }
}
