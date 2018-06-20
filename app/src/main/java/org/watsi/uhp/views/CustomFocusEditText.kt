package org.watsi.uhp.views

import android.content.Context
import android.support.v7.widget.AppCompatEditText
import android.util.AttributeSet
import android.view.KeyEvent

/**
 * Custom EditText that clears focus when the keyboard is hidden via back/down button press. The
 * default behavior does not clear focus.
 *
 * This custom view also cannot use the shorter @JvmOverloads constructor syntax used
 * elsewhere since EditTexts require calling their parent via `super` to initialize their styles.
 * See https://antonioleiva.com/custom-views-android-kotlin/ for more info.
 */
class CustomFocusEditText : AppCompatEditText {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyle: Int) : super(context, attrs, defStyle)

    override fun onKeyPreIme(keyCode: Int, event: KeyEvent): Boolean {
        return if (event.keyCode == KeyEvent.KEYCODE_BACK) {
            clearFocus()
            true
        } else super.dispatchKeyEvent(event)
    }
}
