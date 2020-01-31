package org.watsi.uhp.managers

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

class AndroidKeyboardManager(context: Context) : KeyboardManager {

    private val inputMethodManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager

    /**
     * Request focus before showing keyboard because there are no cases where it makes sense
     * to show a keyboard without a corresponding focused view that is accepting the input
     */
    override fun showKeyboard(view: View) {
        if (view.requestFocus()) {
            inputMethodManager.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
        }
    }

    override fun hideKeyboard(view: View) {
        inputMethodManager.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }
}
