package org.watsi.uhp.helpers

import android.text.Editable
import android.text.TextWatcher

object LayoutHelper {
    fun OnChangedListener(body: (text: String) -> Unit): TextWatcher {
        return (object: TextWatcher {
            override fun afterTextChanged(p0: Editable?) { Unit }
            override fun beforeTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) { Unit }
            override fun onTextChanged(charSequence: CharSequence, p1: Int, p2: Int, p3: Int) {
                body(charSequence.toString())
            }
        })
    }
}
