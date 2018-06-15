package org.watsi.uhp.views

import android.content.Context
import android.util.AttributeSet
import android.view.LayoutInflater
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView

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
}
