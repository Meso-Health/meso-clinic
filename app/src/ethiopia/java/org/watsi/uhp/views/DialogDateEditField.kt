package org.watsi.uhp.views

import android.content.Context
import android.support.constraint.ConstraintLayout
import android.support.v7.app.AlertDialog
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import kotlinx.android.synthetic.main.view_dialog_date_edit_field.view.date_value
import kotlinx.android.synthetic.main.view_dialog_date_edit_field.view.field_label
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.uhp.R
import org.watsi.uhp.fragments.ReceiptFragment
import org.watsi.uhp.helpers.EthiopianDateHelper

class DialogDateEditField @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        LayoutInflater.from(context).inflate(R.layout.view_dialog_date_edit_field, this, true)
        val customAttributes = context.obtainStyledAttributes(attrs, R.styleable.DialogDateEditField)
        field_label.text = customAttributes.getString(R.styleable.DialogDateEditField_label)
        customAttributes.recycle()
    }

    private fun setDate(gregorianDate: LocalDate, clock: Clock) {
        val dateString = EthiopianDateHelper.formatAsEthiopianDate(gregorianDate)
        date_value.setText(if (gregorianDate.isEqual(LocalDate.now(clock.zone))) {
            resources.getString(R.string.today_wrapper, dateString)
        } else {
            dateString
        })
    }

    fun setLabel(label: String) {
        field_label.text = label
    }

    fun setUp(
        initialGregorianValue: LocalDate,
        clock: Clock,
        onDateSelected: ((gregorianDate: LocalDate) -> Unit)
    ) {
        setDate(initialGregorianValue, clock)

        // Set up the dialog
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ethiopian_date_picker, null)
        val daySpinner = dialogView.findViewById<View>(R.id.day_spinner) as SpinnerField
        val monthSpinner = dialogView.findViewById<View>(R.id.month_spinner) as SpinnerField
        val yearSpinner = dialogView.findViewById<View>(R.id.year_spinner) as SpinnerField

        val initialEthiopianValue = EthiopianDateHelper.toEthiopianDate(initialGregorianValue)
        val todayDate = EthiopianDateHelper.toEthiopianDate(LocalDate.now(clock.zone))

        val dayAdapter = SpinnerField.createAdapter(
            context, (1..initialEthiopianValue.day).map { it.toString() })
        val monthAdapter = SpinnerField.createAdapter(
            context, (1..initialEthiopianValue.month).map { it.toString() })
        val yearAdapter = SpinnerField.createAdapter(
            context, (ReceiptFragment.DATE_PICKER_START_YEAR..todayDate.year).map { it.toString() })

        daySpinner.setUpWithoutPrompt(dayAdapter, initialEthiopianValue.day - 1, { /* No-op */ } )
        monthSpinner.setUpWithoutPrompt(monthAdapter, initialEthiopianValue.month - 1, { monthString ->
            val daysToShow = EthiopianDateHelper.daysInMonthNotInFuture(
                yearSpinner.getSelectedItem().toInt(), monthString.toInt(), todayDate)

            dayAdapter.clear()
            dayAdapter.addAll((1..daysToShow).map { it.toString() })
        })
        yearSpinner.setUpWithoutPrompt(yearAdapter, initialEthiopianValue.year - ReceiptFragment.DATE_PICKER_START_YEAR, { yearString ->
            // Save the currently selected month in case the list shrinks
            var selectedMonth = monthSpinner.getSelectedItem().toInt()

            val monthsToShow = EthiopianDateHelper.monthsInYearNotInFuture(yearString.toInt(), todayDate)
            monthAdapter.clear()
            monthAdapter.addAll((1..monthsToShow).map { it.toString() })

            // The following code makes sure our selectedMonth is not larger than the list of months.
            // The Android spinner will do this automatically for us: if we reduce the adapter
            // to a smaller list than the selected index, it will automatically select the highest index
            // in the list. However, this has not happened yet, so we need to calculate this ourselves
            // to calculate the appropriate daysToShow value.
            if (selectedMonth > monthsToShow) selectedMonth = monthsToShow

            val daysToShow = EthiopianDateHelper.daysInMonthNotInFuture(
                yearString.toInt(), selectedMonth, todayDate)
            dayAdapter.clear()
            dayAdapter.addAll((1..daysToShow).map { it.toString() })
        })

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.dialog_save) { _, _ ->
            val ethiopianDate = EthiopianDateHelper.EthiopianDate(
                yearSpinner.getSelectedItem().toInt(),
                monthSpinner.getSelectedItem().toInt(),
                daySpinner.getSelectedItem().toInt()
            )
            val gregorianDate = EthiopianDateHelper.fromEthiopianDate(ethiopianDate)

            setDate(gregorianDate, clock)
            onDateSelected(gregorianDate)
        }
        builder.setNegativeButton(R.string.dialog_cancel) { _, _ -> /* No-Op */ }

        val dateDialog = builder.create()

        date_value.isFocusable = false
        date_value.inputType = 0
        // Set up the onclick listener
        date_value.setOnClickListener {
            daySpinner.setSelectedItem(initialEthiopianValue.day - 1)
            monthSpinner.setSelectedItem(initialEthiopianValue.month - 1)
            yearSpinner.setSelectedItem(initialEthiopianValue.year - ReceiptFragment.DATE_PICKER_START_YEAR)

            dateDialog.show()
        }
    }
}
