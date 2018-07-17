package org.watsi.uhp.fragments

import android.support.v7.app.AlertDialog
import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.ethiopia.fragment_receipt.date_container
import kotlinx.android.synthetic.ethiopia.fragment_receipt.date_label
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_label
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.encounter_items_label
import kotlinx.android.synthetic.ethiopia.fragment_receipt.encounter_items_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.save_button
import kotlinx.android.synthetic.ethiopia.fragment_receipt.total_price
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.utils.DateUtils
import org.watsi.uhp.R
import org.watsi.uhp.R.plurals.diagnosis_count
import org.watsi.uhp.R.plurals.receipt_line_item_count
import org.watsi.uhp.R.string.today_wrapper
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.ReceiptListItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.EthiopianDateHelper
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.utils.CurrencyUtil
import org.watsi.uhp.viewmodels.ReceiptViewModel
import org.watsi.uhp.views.SpinnerField
import javax.inject.Inject

class ReceiptFragment : DaggerFragment(), NavigationManager.HandleOnBack {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock

    lateinit var viewModel: ReceiptViewModel
    lateinit var receiptItemAdapter: ReceiptListItemAdapter
    lateinit var encounterFlowState: EncounterFlowState

    companion object {
        const val PARAM_ENCOUNTER = "encounter"
        const val DATE_PICKER_START_YEAR = 1850

        fun forEncounter(encounter: EncounterFlowState): ReceiptFragment {
            val fragment = ReceiptFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ReceiptViewModel::class.java)
        viewModel.getObservable(encounterFlowState.encounter.occurredAt, encounterFlowState.encounter.backdatedOccurredAt)
            .observe(this, Observer { it?.let { viewState ->
                val dateString = EthiopianDateHelper.formatEthiopianDate(viewState.occurredAt, clock)
                date_label.text = if (DateUtils.isToday(viewState.occurredAt, clock)) {
                    resources.getString(today_wrapper, dateString)
                } else {
                    dateString
                }
            }
            })

        receiptItemAdapter = ReceiptListItemAdapter(encounterFlowState.encounterItems)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.receipt_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_receipt, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        diagnoses_label.text = resources.getQuantityString(
            diagnosis_count, encounterFlowState.diagnoses.size, encounterFlowState.diagnoses.size)
        encounter_items_label.text = resources.getQuantityString(
            receipt_line_item_count, encounterFlowState.encounterItems.size, encounterFlowState.encounterItems.size)
        total_price.text = getString(R.string.price, CurrencyUtil.formatMoney(encounterFlowState.price()))

        if (encounterFlowState.diagnoses.isNotEmpty()) {
            diagnoses_list.visibility = View.VISIBLE
            diagnoses_list.text = encounterFlowState.diagnoses.map { it.description }.joinToString(", ")
        }

        date_container.setOnClickListener {
            launchBackdateDialog()
        }

        RecyclerViewHelper.setRecyclerView(encounter_items_list, receiptItemAdapter, context, false)

        save_button.setOnClickListener {
            submitEncounter()
        }
    }

    private fun launchBackdateDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ethiopian_date_picker, null)
        val daySpinner = dialogView.findViewById<View>(R.id.day_spinner) as SpinnerField
        val monthSpinner = dialogView.findViewById<View>(R.id.month_spinner) as SpinnerField
        val yearSpinner = dialogView.findViewById<View>(R.id.year_spinner) as SpinnerField

        val occurredAtDate = EthiopianDateHelper.toEthiopianDate(
            viewModel.occurredAt() ?: Instant.now(),
            clock
        )
        val dayAdapter = SpinnerField.createAdapter(
            context, (1..EthiopianDateHelper.daysInMonth(occurredAtDate.year, occurredAtDate.month)).map { it.toString() })
        val monthAdapter = SpinnerField.createAdapter(
            context, (1..EthiopianDateHelper.MONTHS_IN_YEAR).map { it.toString() })
        val yearAdapter = SpinnerField.createAdapter(
            context, (DATE_PICKER_START_YEAR..occurredAtDate.year).map { it.toString() })

        daySpinner.setUpSpinner(dayAdapter, occurredAtDate.day - 1, { /* No-op */ } )
        monthSpinner.setUpSpinner(monthAdapter, occurredAtDate.month - 1, { monthString ->
            dayAdapter.clear()
            dayAdapter.addAll((1..EthiopianDateHelper.daysInMonth(
                yearSpinner.getSelectedItem().toInt(), monthString.toInt()))
                .map { it.toString() })
        })
        yearSpinner.setUpSpinner(yearAdapter, occurredAtDate.year - DATE_PICKER_START_YEAR, { yearString ->
            dayAdapter.clear()
            dayAdapter.addAll((1..EthiopianDateHelper.daysInMonth(
                yearString.toInt(), monthSpinner.getSelectedItem().toInt()))
                .map { it.toString() })
        })

        val builder = AlertDialog.Builder(context)
        builder.setView(dialogView)
        builder.setPositiveButton(R.string.eth_datepicker_save) { dialog, _ ->
            val backdatedDateTime = EthiopianDateHelper.toInstant(
                yearSpinner.getSelectedItem().toInt(),
                monthSpinner.getSelectedItem().toInt(),
                daySpinner.getSelectedItem().toInt(),
                0, 0, 0, 0, // Arbitrarily choose midnight, since time isn't specified
                clock
            )

            viewModel.updateBackdatedOccurredAt(backdatedDateTime)
        }
        builder.setNegativeButton(R.string.eth_datepicker_cancel) { dialog, _ -> /* No-Op */ }

        builder.create().show()
    }

    private fun submitEncounter() {
        viewModel.submitEncounter(encounterFlowState).subscribe({
            navigationManager.popTo(CurrentPatientsFragment.withSnackbarMessage(
                getString(R.string.encounter_submitted)
            ))
        }, {
            logger.error(it)
        })
    }

    override fun onBack(): Single<Boolean> {
        return Single.fromCallable {
            viewModel.updateEncounterWithDate(encounterFlowState)
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
