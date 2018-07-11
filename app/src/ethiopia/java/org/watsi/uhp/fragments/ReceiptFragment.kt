package org.watsi.uhp.fragments

import android.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.ethiopia.fragment_receipt.date_edit
import kotlinx.android.synthetic.ethiopia.fragment_receipt.date_label
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_label
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.encounter_items_label
import kotlinx.android.synthetic.ethiopia.fragment_receipt.encounter_items_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.save_button
import kotlinx.android.synthetic.ethiopia.fragment_receipt.total_price
import org.threeten.bp.Clock
import org.threeten.bp.LocalDateTime
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.MutableEncounterWithItemsAndForms
import org.watsi.domain.utils.DateUtils
import org.watsi.uhp.R
import org.watsi.uhp.R.plurals.diagnosis_count
import org.watsi.uhp.R.plurals.receipt_line_item_count
import org.watsi.uhp.R.string.date_and_time
import org.watsi.uhp.R.string.price_with_currency
import org.watsi.uhp.R.string.today_wrapper
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.ReceiptListItemAdapter
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.ReceiptViewModel
import java.text.NumberFormat
import javax.inject.Inject

class ReceiptFragment : DaggerFragment(), NavigationManager.HandleOnBack {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock

    lateinit var viewModel: ReceiptViewModel
    lateinit var receiptItemAdapter: ReceiptListItemAdapter
    lateinit var encounterBuilder: MutableEncounterWithItemsAndForms

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: MutableEncounterWithItemsAndForms): ReceiptFragment {
            val fragment = ReceiptFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encounterBuilder = arguments.getSerializable(PARAM_ENCOUNTER) as MutableEncounterWithItemsAndForms
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ReceiptViewModel::class.java)
        viewModel.getObservable(encounterBuilder.encounter.occurredAt, encounterBuilder.encounter.backdatedOccurredAt)
            .observe(this, Observer { it?.let { viewState ->
                val date_string = DateUtils.formatLocalDate(viewState.occurredAt.atZone(clock.zone).toLocalDate())
                val time_string = DateUtils.formatLocalTime(viewState.occurredAt.atZone(clock.zone).toLocalDateTime())
                date_label.text = if (DateUtils.isToday(viewState.occurredAt, clock)) {
                    resources.getString(today_wrapper, date_string)
                } else {
                    resources.getString(date_and_time, date_string, time_string)
                }
            }
            })

        receiptItemAdapter = ReceiptListItemAdapter(encounterBuilder.encounterItems)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.receipt_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_receipt, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        diagnoses_label.text = resources.getQuantityString(
            diagnosis_count, encounterBuilder.diagnoses.size, encounterBuilder.diagnoses.size)
        encounter_items_label.text = resources.getQuantityString(
            receipt_line_item_count, encounterBuilder.encounterItems.size, encounterBuilder.encounterItems.size)
        total_price.text = getString(price_with_currency, NumberFormat.getNumberInstance().format(encounterBuilder.price()))

        if (encounterBuilder.diagnoses.isNotEmpty()) {
            diagnoses_list.visibility = View.VISIBLE
            diagnoses_list.text = encounterBuilder.diagnoses.map { it.description }.joinToString(", ")
        }

        date_edit.setOnClickListener {
            launchBackdateDialog()
        }

        RecyclerViewHelper.setRecyclerView(encounter_items_list, receiptItemAdapter, context, false)

        save_button.setOnClickListener {
            submitEncounter()
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

    private fun submitEncounter() {
        viewModel.submitEncounter(encounterBuilder).subscribe({
            navigationManager.popTo(CurrentPatientsFragment.withSnackbarMessage(
                getString(R.string.encounter_submitted)
            ))
        }, {
            logger.error(it)
        })
    }

    override fun onBack(): Single<Boolean> {
        return Single.fromCallable {
            viewModel.updateEncounterWithDate(encounterBuilder)
            true
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            android.R.id.home -> {
                navigationManager.goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
