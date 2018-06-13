package org.watsi.uhp.views

import android.content.Context
import android.text.Editable
import android.text.TextWatcher
import android.util.AttributeSet
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RelativeLayout
import kotlinx.android.synthetic.main.view_toolbar_search.view.back_button
import kotlinx.android.synthetic.main.view_toolbar_search.view.clear_button
import kotlinx.android.synthetic.main.view_toolbar_search.view.scan_button
import kotlinx.android.synthetic.main.view_toolbar_search.view.search_text
import org.watsi.uhp.managers.KeyboardManager

class ToolbarSearch @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : RelativeLayout(context, attrs, defStyleAttr) {

    var keyboardManager: KeyboardManager? = null

    init {
        post {
            focus()
        }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()

        search_text.addTextChangedListener(object: TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // no-op
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // no-op
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s != null && s.isNotEmpty()) {
                    scan_button.visibility = View.GONE
                    clear_button.visibility = View.VISIBLE
                } else {
                    scan_button.visibility = View.VISIBLE
                    clear_button.visibility = View.GONE
                }
            }
        })

        clear_button.setOnClickListener {
            clear()
            focus()
        }
    }

    fun onBack(callback: () -> Unit) {
        back_button.setOnClickListener {
            keyboardManager?.hideKeyboard(search_text)
            callback()
        }
    }

    fun onSearch(callback: (query: String) -> Unit) {
        search_text.setOnEditorActionListener { view, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                callback(view.text.toString())
            }
            keyboardManager?.hideKeyboard(view)
            true
        }
    }

    fun onScan(callback: () -> Unit) {
        scan_button.setOnClickListener {
            callback()
        }
    }

    fun clear() {
        search_text.text.clear()
    }

    private fun focus() {
        search_text.requestFocus()
        keyboardManager?.showKeyboard(search_text)
    }
}
