package org.watsi.uhp.helpers

import android.content.Context
import android.support.design.widget.Snackbar
import android.view.View
import android.widget.TextView
import org.watsi.uhp.R

object SnackbarHelper {
    fun show(view: View, context: Context, messageResource: Int) {
        show(view, context, context.getString(messageResource))
    }

    fun show(view: View, context: Context, message: String) {
        val snackbar = Snackbar.make(view, message, Snackbar.LENGTH_LONG)
        val textView = snackbar.view.findViewById<TextView>(android.support.design.R.id.snackbar_text)
        textView.setTextColor(context.getColor(R.color.white))
        snackbar.view.setBackgroundColor(context.getColor(R.color.gray8))
        snackbar.show()
    }
}