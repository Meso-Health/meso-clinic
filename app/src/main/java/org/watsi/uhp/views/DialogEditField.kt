package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.design.widget.TextInputEditText
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.EditorInfo
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.action
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.field_label
import kotlinx.android.synthetic.main.view_dialog_edit_field.view.input_value
import org.watsi.uhp.R
import org.watsi.uhp.managers.KeyboardManager

/**
 * View for displaying a Member field based on the Material design text input that exposes an API
 * for setting up a click handler that updates the value with a Dialog and handles updating the
 * corresponding MemberModel with a callback parameter.
 *
 * Supports editing the field with a EditText input using configureEditTextDialog or via a list
 * of options via configureOptionsDialog.
 */
class DialogEditField @JvmOverloads constructor(
        context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    private var dialogEditTextInputType: Int = 1
    private var value: String? = null

    init {
        LayoutInflater.from(context).inflate(R.layout.view_dialog_edit_field, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.DialogEditField)
        field_label.text = customAttributes.getString(R.styleable.DialogEditField_label)
        dialogEditTextInputType = customAttributes.getInt(
                R.styleable.DialogEditField_android_inputType, 1)
        customAttributes.recycle()
    }

    /**
     * Re-renders the field with the updated value.
     */
    fun setValue(newValue: String?) {
        value = newValue
         if (value.isNullOrBlank()) {
             field_label.visibility = View.INVISIBLE
             input_value.text = field_label.text
             input_value.setTextColor(context.getColor(R.color.gray6))
             action.text = resources.getString(R.string.edit_input_add)
        } else {
             field_label.visibility = View.VISIBLE
             input_value.text = value
             input_value.setTextColor(context.getColor(R.color.gray9))
             action.text = resources.getString(R.string.edit_input_edit)
        }
    }

    /**
     * Configures the field to respond to click events with an AlertDialog that supports editing
     * the value via an EditText and handles the new value via the handleNewValue callback
     */
    fun configureEditTextDialog(keyboardManager: KeyboardManager,
                                handleNewValue: (value: String, dialog: AlertDialog) -> Unit,
                                multiline: Boolean = false) {
        setOnClickListener {
            launchEditTextDialog(keyboardManager, handleNewValue, multiline)
        }
    }

    fun launchEditTextDialog(keyboardManager: KeyboardManager,
                             handleNewValue: (value: String, dialog: AlertDialog) -> Unit,
                             multiline: Boolean = false,
                             defaultValue: (value: String?) -> String? = { value -> value }
    ) {
        val dialogBuilder = AlertDialog.Builder(context)
        val editTextLayout = LayoutInflater.from(context).inflate(R.layout.dialog_edit_text, null)
        val editText = editTextLayout.findViewById<TextInputEditText>(R.id.dialog_input)
        editText.setText(defaultValue(value))
        editText.inputType = dialogEditTextInputType
        if (multiline) {
            editText.setHorizontallyScrolling(false)
            editText.maxLines = 20
        }
        dialogBuilder.setView(editTextLayout)
        dialogBuilder.setTitle(field_label.text)
        dialogBuilder.setPositiveButton(R.string.modal_save, { _, _ ->
            // no-op, handled in onClickListener below
        })
        dialogBuilder.setNegativeButton(R.string.modal_cancel, { dialogInterface, _ ->
            (dialogInterface as AlertDialog).dismiss()
        })
        val dialog = dialogBuilder.create()
        editText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                handleNewValue(editText.text.toString(), dialog)
                true
            } else {
                false
            }
        }
        dialog.setOnShowListener {
            // need to use post for showKeyboard to work reliably
            editText.post { keyboardManager.showKeyboard(editText) }
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                handleNewValue(editText.text.toString(), dialog)
            }
        }
        dialog.show()
    }

    /**
     * Configures the field to respond to click events with a single-choice Dialog corresponding
     * to the provided options and handles the selected option via the handleSelected callback.
     */
    fun configureOptionsDialog(options: Array<String>,
                               handleSelected: (which: Int) -> Unit) {
        setOnClickListener {
            val selectedIndex = if (options.indexOf(value) >= 0) {
                options.indexOf(value)
            } else {
                // hack to select Other when editing preferred language
                options.size - 1
            }
            AlertDialog.Builder(context)
                    .setTitle(field_label.text)
                    .setSingleChoiceItems(options, selectedIndex, { _, _ -> /* no-op */})
                    .setPositiveButton(R.string.modal_save, { di, _ ->
                        handleSelected((di as AlertDialog).listView.checkedItemPosition)
                    })
                    .setNegativeButton(R.string.modal_cancel, { dialogInterface, _ ->
                        (dialogInterface as AlertDialog).dismiss()
                    })
                    .show()
        }
    }
}
