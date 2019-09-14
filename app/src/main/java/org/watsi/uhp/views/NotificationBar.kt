package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.android.synthetic.main.notification_bar.view.notification_btn

import org.watsi.uhp.R

class NotificationBar(context: Context, attrs: AttributeSet) : LinearLayout(context, attrs) {

    init {
        LayoutInflater.from(context).inflate(R.layout.notification_bar, this)

        val notificationMessage = findViewById<TextView>(R.id.notification_message)
        val notificationBtn = findViewById<Button>(R.id.notification_btn)

        val ta = getContext().obtainStyledAttributes(attrs, R.styleable.NotificationBar)

        try {
            val message = ta.getString(R.styleable.NotificationBar_message)
            val action = ta.getString(R.styleable.NotificationBar_action)

            notificationMessage.text = message
            notificationBtn.text = action
        } finally {
            ta.recycle()
        }
    }

    override fun setOnClickListener(listener: OnClickListener?) {
        notification_btn.setOnClickListener(listener)
    }

    fun setMessage(message: String, messageColor: Int? = null, backgroundColor: Int? = null) {
        val notificationMessage = findViewById<TextView>(R.id.notification_message)
        val component = findViewById<ConstraintLayout>(R.id.notification_banner)
        notificationMessage.text = message
        messageColor?.let { notificationMessage.setTextColor(it) }
        backgroundColor?.let { component.setBackgroundColor(backgroundColor)}
    }
}
