package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import kotlinx.android.synthetic.main.view_spinner_field.view.field_label
import kotlinx.android.synthetic.main.view_spinner_field.view.spinner
import kotlinx.android.synthetic.main.view_spinner_field.view.spinner_error_message
import org.watsi.uhp.R


class SpinnerField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {
    init {
        LayoutInflater.from(context).inflate(R.layout.view_spinner_field, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.SpinnerField)
        field_label.text = customAttributes.getString(R.styleable.SpinnerField_label)

        customAttributes.recycle()
    }

    companion object {
        fun createAdapter(context: Context, choices: List<String>): ArrayAdapter<String> {
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, choices)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            return adapter
        }

        class SpinnerWithPromptAdapter(
            private val mContext: Context?,
            resource: Int,
            private val choices: List<String>
        ) : ArrayAdapter<String>(mContext, resource, choices) {
            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
                val view = LayoutInflater.from(mContext).inflate(android.R.layout.simple_spinner_dropdown_item, parent, false) as TextView
                view.text = choices[position]
                if (position == 0) { view.setTextColor(context.getColor(R.color.gray6)) }
                return view
            }
        }
    }

    /**
     * Set up the spinner with no prompt
     *
     * @param choices The mutable adapter of choices to populate the spinner
     * @param initialChoiceIndex The initial choice to be selected by default when the spinner loads
     * @param onItemSelected The code to execute when an item is selected
     */
    fun setUpWithoutPrompt(adapter: ArrayAdapter<String>,
                           initialChoiceIndex: Int?,
                           onItemSelected: (choice: String) -> Unit) {
        spinner.adapter = adapter
        initialChoiceIndex?.let { spinner.setSelection(it) }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */ }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedString = adapter.getItem(position)
                onItemSelected(selectedString)
            }
        }
        setUpSpinnerOnTouchListener()
    }


    /**
     * Sets up the spinner with a prompt.
     *
     * @param choices The mutable adapter of choices to populate the spinner
     * @param initialChoiceIndex The initial choice to be selected by default when the spinner loads
     * @param onItemSelected The code to execute when an item is selected
     * @param promptString The prompt that appears at the top of the dropdown
     * @param onPromptSelected The code to execute when the prompt string is selected
     */
    fun setUpWithPrompt(
        choices: List<String>,
        initialChoiceIndex: Int?,
        onItemSelected: (index: Int) -> Unit,
        promptString: String,
        onPromptSelected: (() -> Unit)
    ) {
        val choicesForAdapter = listOfNotNull(promptString) + choices
        val adapter = SpinnerWithPromptAdapter(context, android.R.layout.simple_spinner_item, choicesForAdapter)
        spinner.adapter = adapter

        initialChoiceIndex?.let {
            spinner.setSelection(it + 1) // Add one because the 0th choice is the prompt.
        }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */ }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedString = adapter.getItem(position)
                if (position == 0 && selectedString == promptString) {
                    // Not sure why but this line below is needed to set the prompt to the spinner field to grey if it's the prompt.
                    (view as TextView?)?.setTextColor(context.getColor(R.color.gray6))
                    onPromptSelected()
                } else {
                    // We need to offset by 1 because promptString is the 0th choice.
                    onItemSelected(position - 1)
                }
            }
        }
        setUpSpinnerOnTouchListener()
    }

    private fun setUpSpinnerOnTouchListener() {
        spinner.setOnTouchListener { _, motionEvent ->
            if (motionEvent.action == MotionEvent.ACTION_UP) {
                this.requestFocus()
                spinner.performClick()
            }
            true
        }
    }

    fun getSelectedItem(): String {
        return spinner.selectedItem.toString()
    }

    fun setSelectedItem(position: Int) {
        spinner.setSelection(position)
    }

    fun setError(errorMessage: String?) {
        spinner_error_message.error = errorMessage
    }
}
