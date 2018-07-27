package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import io.reactivex.Single
import kotlinx.android.synthetic.ethiopia.fragment_receipt.date_container
import kotlinx.android.synthetic.ethiopia.fragment_receipt.date_label
import kotlinx.android.synthetic.ethiopia.fragment_receipt.drug_and_supply_items_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.drug_and_supply_none
import kotlinx.android.synthetic.ethiopia.fragment_receipt.drug_and_supply_line_divider
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_label
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.gender_and_age
import kotlinx.android.synthetic.ethiopia.fragment_receipt.lab_items_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.lab_line_divider
import kotlinx.android.synthetic.ethiopia.fragment_receipt.lab_none
import kotlinx.android.synthetic.ethiopia.fragment_receipt.medical_record_number
import kotlinx.android.synthetic.ethiopia.fragment_receipt.membership_number
import kotlinx.android.synthetic.ethiopia.fragment_receipt.save_button
import kotlinx.android.synthetic.ethiopia.fragment_receipt.service_items_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.service_line_divider
import kotlinx.android.synthetic.ethiopia.fragment_receipt.service_none
import kotlinx.android.synthetic.ethiopia.fragment_receipt.total_price
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.utils.DateUtils
import org.watsi.uhp.R
import org.watsi.uhp.R.plurals.diagnosis_count
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
    lateinit var serviceReceiptItemAdapter: ReceiptListItemAdapter
    lateinit var labReceiptItemAdapter: ReceiptListItemAdapter
    lateinit var drugAndSupplyReceiptItemAdapter: ReceiptListItemAdapter
    lateinit var alertDialog: AlertDialog
    lateinit var encounterFlowState: EncounterFlowState

    lateinit var daySpinner: SpinnerField
    lateinit var monthSpinner: SpinnerField
    lateinit var yearSpinner: SpinnerField

    companion object {
        const val PARAM_ENCOUNTER = "encounter"
        const val DATE_PICKER_START_YEAR = 2008

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

        val services = encounterFlowState.getEncounterItemsOfType(Billable.Type.SERVICE)
        val labs = encounterFlowState.getEncounterItemsOfType(Billable.Type.LAB)
        val drugsAndSupplies = encounterFlowState.getEncounterItemsOfType(Billable.Type.SUPPLY)
                .plus(encounterFlowState.getEncounterItemsOfType(Billable.Type.DRUG))

        serviceReceiptItemAdapter = ReceiptListItemAdapter(services)
        labReceiptItemAdapter = ReceiptListItemAdapter(labs)
        drugAndSupplyReceiptItemAdapter = ReceiptListItemAdapter(drugsAndSupplies)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.receipt_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_receipt, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val genderAndAgeText = encounterFlowState.member?.formatAgeAndGender(clock)

        membership_number.text = encounterFlowState.member?.membershipNumber
        gender_and_age.text = genderAndAgeText
        medical_record_number.text = encounterFlowState.member?.medicalRecordNumber
        diagnoses_label.text = resources.getQuantityString(
            diagnosis_count, encounterFlowState.diagnoses.size, encounterFlowState.diagnoses.size)
        total_price.text = getString(R.string.price, CurrencyUtil.formatMoney(encounterFlowState.price()))

        if (encounterFlowState.diagnoses.isNotEmpty()) {
            diagnoses_list.visibility = View.VISIBLE
            diagnoses_list.text = encounterFlowState.diagnoses.map { it.description }.joinToString(", ")
        }

        setUpDialog()

        date_container.setOnClickListener {
            launchBackdateDialog()
        }

        if (serviceReceiptItemAdapter.itemCount == 0) {
            service_none.visibility = View.VISIBLE
            service_line_divider.visibility = View.VISIBLE
        } else {
            RecyclerViewHelper.setRecyclerView(service_items_list, serviceReceiptItemAdapter, context, false)
            service_items_list.visibility = View.VISIBLE
        }

        if (labReceiptItemAdapter.itemCount == 0) {
            lab_none.visibility = View.VISIBLE
            lab_line_divider.visibility = View.VISIBLE
        } else {
            RecyclerViewHelper.setRecyclerView(lab_items_list, labReceiptItemAdapter, context, false)
            lab_items_list.visibility = View.VISIBLE
        }

        if (drugAndSupplyReceiptItemAdapter.itemCount == 0) {
            drug_and_supply_none.visibility = View.VISIBLE
            drug_and_supply_line_divider.visibility = View.VISIBLE
        } else {
            RecyclerViewHelper.setRecyclerView(drug_and_supply_items_list, drugAndSupplyReceiptItemAdapter, context, false)
            drug_and_supply_items_list.visibility = View.VISIBLE
        }

        save_button.setOnClickListener {
            submitEncounter()
        }
    }

    private fun launchBackdateDialog() {
        val occurredAtDate = EthiopianDateHelper.toEthiopianDate(
            viewModel.occurredAt() ?: Instant.now(),
            clock
        )
        daySpinner.setSelectedItem(occurredAtDate.day - 1)
        monthSpinner.setSelectedItem(occurredAtDate.month - 1)
        yearSpinner.setSelectedItem(occurredAtDate.year - DATE_PICKER_START_YEAR)

        alertDialog.show()
    }

    private fun setUpDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_ethiopian_date_picker, null)
        daySpinner = dialogView.findViewById<View>(R.id.day_spinner) as SpinnerField
        monthSpinner = dialogView.findViewById<View>(R.id.month_spinner) as SpinnerField
        yearSpinner = dialogView.findViewById<View>(R.id.year_spinner) as SpinnerField

        val occurredAtDate = EthiopianDateHelper.toEthiopianDate(
            viewModel.occurredAt() ?: Instant.now(),
            clock
        )
        val todayDate = EthiopianDateHelper.toEthiopianDate(Instant.now(), clock)

        val dayAdapter = SpinnerField.createAdapter(
            context, (1..todayDate.day).map { it.toString() })
        val monthAdapter = SpinnerField.createAdapter(
            context, (1..todayDate.month).map { it.toString() })
        val yearAdapter = SpinnerField.createAdapter(
            context, (DATE_PICKER_START_YEAR..todayDate.year).map { it.toString() })

        daySpinner.setUpSpinner(dayAdapter, occurredAtDate.day - 1, { /* No-op */ } )
        monthSpinner.setUpSpinner(monthAdapter, occurredAtDate.month - 1, { monthString ->
            val daysToShow = EthiopianDateHelper.daysInMonthNotInFuture(
                yearSpinner.getSelectedItem().toInt(), monthString.toInt(), todayDate)

            dayAdapter.clear()
            dayAdapter.addAll((1..daysToShow).map { it.toString() })
        })
        yearSpinner.setUpSpinner(yearAdapter, occurredAtDate.year - DATE_PICKER_START_YEAR, { yearString ->
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

        alertDialog = builder.create()
    }

    private fun submitEncounter() {
        viewModel.submitEncounter(encounterFlowState).subscribe({
            navigationManager.popTo(NewClaimFragment.withSnackbarMessage(
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
