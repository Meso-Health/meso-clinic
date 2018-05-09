package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import kotlinx.android.synthetic.main.view_status_field.view.*
import org.watsi.uhp.R

class StatusField @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_status_field, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.StatusField)
        status_key.text = customAttributes.getString(R.styleable.StatusField_android_text)
        customAttributes.recycle()
    }

    fun setValue(value: String?) {
        status_value.text = value
    }
}