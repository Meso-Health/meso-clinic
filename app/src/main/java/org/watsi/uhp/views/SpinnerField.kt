package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import kotlinx.android.synthetic.main.view_spinner_field.view.field_label
import kotlinx.android.synthetic.main.view_spinner_field.view.spinner
import org.watsi.device.managers.Logger
import org.watsi.uhp.R
import javax.inject.Inject

class SpinnerField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    @Inject lateinit var logger: Logger

    private lateinit var adapter: ArrayAdapter<String>

    companion object {
        fun createAdapter(context: Context, choices: List<String>): ArrayAdapter<String> {
            val adapter = ArrayAdapter(context, android.R.layout.simple_spinner_item, choices)
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            return adapter
        }
    }

    init {
        LayoutInflater.from(context).inflate(R.layout.view_spinner_field, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.SpinnerField)
        field_label.text = customAttributes.getString(R.styleable.SpinnerField_label)

        try {
            val width = customAttributes.getDimensionPixelSize(R.styleable.SpinnerField_dropDownWidth, 0)
            if (width > 0) {
                spinner.dropDownWidth = width
            }
        } catch (e: NumberFormatException) {
            logger.error(e)
        }

        customAttributes.recycle()
    }

    /**
     * Fill and initializes the spinner with an immutable list of choices
     *
     * @param choices The immutable list of choices to populate the spinner
     * @param initialChoice The initial choice to be selected by default when the spinner loads
     * @param onItemSelected The code to execute when an item is selected
     * @param promptString The "unselected" prompt. For example "Select a billable". This should not be a member of the list
     * @param onPromptSelected The code to execute if the prompt is selected. For example, clearing data
     */
    fun setUpSpinner(choices: List<String>,
                     initialChoice: String?,
                     onItemSelected: (choice: String) -> Unit,
                     promptString: String? = null,
                     onPromptSelected: (() -> Unit)? = null) {
        setUpSpinner(createAdapter(context, choices), choices.indexOf(initialChoice),
            onItemSelected, promptString, onPromptSelected)
    }

    /**
     * Fill and initializes the spinner with an mutable adapter of choices
     *
     * @param choices The mutable adapter of choices to populate the spinner
     * @param initialChoice The initial choice to be selected by default when the spinner loads
     * @param onItemSelected The code to execute when an item is selected
     * @param promptString The "unselected" prompt. For example "Select a billable". This should not be a member of the list
     * @param onPromptSelected The code to execute if the prompt is selected. For example, clearing data
     */
    fun setUpSpinner(adapter: ArrayAdapter<String>,
                     initialChoice: Int?,
                     onItemSelected: (choice: String) -> Unit,
                     promptString: String? = null,
                     onPromptSelected: (() -> Unit)? = null) {
        this.adapter = adapter
        spinner.adapter = adapter
        promptString?.let { adapter.insert(promptString, 0) }
        initialChoice?.let { spinner.setSelection(it) }
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) { /* no-op */ }

            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedString = adapter.getItem(position)
                if (selectedString == promptString) {
                    onPromptSelected?.let { onPromptSelected() }
                } else {
                    onItemSelected(selectedString)
                }
            }
        }
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
}
