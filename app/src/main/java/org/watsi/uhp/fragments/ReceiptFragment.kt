package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import android.widget.Toast
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_receipt.date_edit
import kotlinx.android.synthetic.main.fragment_receipt.date_label
import kotlinx.android.synthetic.main.fragment_receipt.diagnoses_label
import kotlinx.android.synthetic.main.fragment_receipt.diagnoses_list
import kotlinx.android.synthetic.main.fragment_receipt.encounter_items_label
import kotlinx.android.synthetic.main.fragment_receipt.encounter_items_list
import kotlinx.android.synthetic.main.fragment_receipt.forms_label
import kotlinx.android.synthetic.main.fragment_receipt.save_button
import kotlinx.android.synthetic.main.fragment_receipt.total_price
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.uhp.R
import org.watsi.uhp.R.plurals.diagnosis_count
import org.watsi.uhp.R.plurals.forms_attached_label
import org.watsi.uhp.R.plurals.receipt_line_item_count
import org.watsi.uhp.R.string.encounter_submitted
import org.watsi.uhp.R.string.price_with_currency
import org.watsi.uhp.R.string.today_wrapper
import org.watsi.uhp.adapters.ReceiptListItemAdapter
import org.watsi.uhp.helpers.DateHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.ReceiptViewModel
import javax.inject.Inject

class ReceiptFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock

    lateinit var viewModel: ReceiptViewModel
    lateinit var encounter: EncounterWithItemsAndForms

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterWithItemsAndForms): ReceiptFragment {
            val fragment = ReceiptFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encounter = arguments.getSerializable(EncounterFormFragment.PARAM_ENCOUNTER) as EncounterWithItemsAndForms
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ReceiptViewModel::class.java)
        viewModel.getObservable(encounter.encounter.occurredAt, encounter.encounter.backdatedOccurredAt)
            .observe(this, Observer { it?.let { viewState ->
                val dateString = DateHelper.formatDateString(viewState.occurredAt, clock)
                date_label.text = if (DateHelper.isToday(viewState.occurredAt, clock)) {
                    resources.getString(today_wrapper, dateString)
                } else {
                    dateString
                }
            }

        })

    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        activity.setTitle(R.string.receipt_fragment_label)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_receipt, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        diagnoses_label.text = resources.getQuantityString(
                diagnosis_count, encounter.diagnoses.size, encounter.diagnoses.size)
        encounter_items_label.text = resources.getQuantityString(
                receipt_line_item_count, encounter.encounterItems.size, encounter.encounterItems.size)
        total_price.text = getString(price_with_currency, encounter.price().toString()) // TODO: format
        forms_label.text = resources.getQuantityString(
                forms_attached_label, encounter.encounterForms.size, encounter.encounterForms.size)

        if (encounter.diagnoses.isNotEmpty()) {
            diagnoses_list.visibility = View.VISIBLE
            diagnoses_list.text = encounter.diagnoses.map { it.description }.joinToString(", ")
        }

        encounter_items_list.adapter = ReceiptListItemAdapter(encounter.encounterItems)
        encounter_items_list.layoutManager = LinearLayoutManager(activity)

        date_edit.setOnClickListener {
            launchBackdateDialog()
        }
        save_button.setOnClickListener {
            submitEncounter(true)
        }
    }

    private fun launchBackdateDialog() {
        val dialogView = activity.layoutInflater.inflate(R.layout.dialog_backdate_encounter, null)
        val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
        val timePicker = dialogView.findViewById<View>(R.id.time_picker) as TimePicker

        datePicker.maxDate = clock.instant().toEpochMilli()

        viewModel.backdatedOccurredAt()?.let { backdatedOccurredAt ->
            if (backdatedOccurredAt) {
                viewModel.occurredAt()?.let { occurredAt ->
                    val ldt = LocalDateTime.ofInstant(occurredAt, clock.zone)
                    datePicker.updateDate(ldt.year, ldt.monthValue - 1, ldt.dayOfMonth) // DatePicker months are zero indexed but LocalDateTime's are not
                    timePicker.hour = ldt.hour
                    timePicker.minute = ldt.minute
                }
            }
        }


        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)

        val dialog = builder.create()

        dialogView.findViewById<View>(R.id.done).setOnClickListener {
            val backdatedLocalDateTime = LocalDateTime.of(datePicker.year,
                datePicker.month + 1, // DatePicker months are zero indexed but LocalDateTime's are not
                datePicker.dayOfMonth,
                timePicker.hour,
                timePicker.minute
            )
            viewModel.updateBackdatedOccurredAt(
                backdatedLocalDateTime.toInstant(clock.zone.rules.getOffset(backdatedLocalDateTime)))
            dialog.dismiss()
        }

        dialogView.findViewById<View>(R.id.cancel).setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun submitEncounter(copaymentPaid: Boolean) {
        viewModel.submitEncounter(encounter).subscribe({
            navigationManager.popTo(CurrentPatientsFragment())
            Toast.makeText(activity, getString(encounter_submitted), Toast.LENGTH_LONG).show()
        }, {
            logger.error(it)
        })
    }

    override fun onPrepareOptionsMenu(menu: Menu) {
        menu.findItem(R.id.menu_submit_without_copayment).isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_submit_without_copayment -> {
                submitEncounter(false)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
