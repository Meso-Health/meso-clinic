package org.watsi.uhp.custom_components;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import org.watsi.uhp.R;

public class NotificationBar extends LinearLayout {

    // displayed message
    private String message;

    // displayed button text
    private String action;

    // internal components
    private TextView notificationMessage;
    private Button notificationBtn;

    public NotificationBar(Context context, AttributeSet attrs)
    {
        super(context, attrs);
        initControl(context, attrs);
    }

    private void initControl(Context context, AttributeSet attrs)
    {
        LayoutInflater inflater = (LayoutInflater)
                context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        inflater.inflate(R.layout.notification_bar, this);

        notificationMessage = (TextView)findViewById(R.id.notification_message);
        notificationBtn = (Button)findViewById(R.id.notification_btn);

        TypedArray ta = getContext().obtainStyledAttributes(attrs, R.styleable.NotificationBar);

        try {
            message = ta.getString(R.styleable.NotificationBar_message);
            action = ta.getString(R.styleable.NotificationBar_action);

            notificationMessage.setText(message);
            notificationBtn.setText(action);
        } finally {
            ta.recycle();
        }
    }

    public void setOnActionClickListener(OnClickListener l) {
        notificationBtn.setOnClickListener(l);
    }
}
