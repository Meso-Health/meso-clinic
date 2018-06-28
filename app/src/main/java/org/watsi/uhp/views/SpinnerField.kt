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
import org.watsi.uhp.R

class SpinnerField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

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

        customAttributes.recycle()
    }

    fun setUpSpinner(choices: List<String>,
                     initialChoice: String?,
                     onItemSelected: (choice: String) -> Unit,
                     promptString: String? = null,
                     onPromptSelected: (() -> Unit)? = null) {
        setUpSpinner(createAdapter(context, choices), choices.indexOf(initialChoice),
            onItemSelected, promptString, onPromptSelected)
    }

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
