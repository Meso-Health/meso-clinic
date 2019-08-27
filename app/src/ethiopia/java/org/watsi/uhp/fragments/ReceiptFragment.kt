package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.support.v7.app.AlertDialog
import android.text.Editable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.ethiopia.fragment_receipt.adjudication_comments_container
import kotlinx.android.synthetic.ethiopia.fragment_receipt.branch_comment_date
import kotlinx.android.synthetic.ethiopia.fragment_receipt.branch_comment_text
import kotlinx.android.synthetic.ethiopia.fragment_receipt.claim_id
import kotlinx.android.synthetic.ethiopia.fragment_receipt.claim_id_container
import kotlinx.android.synthetic.ethiopia.fragment_receipt.comment_container
import kotlinx.android.synthetic.ethiopia.fragment_receipt.comment_text
import kotlinx.android.synthetic.ethiopia.fragment_receipt.date_container
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_edit
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.diagnoses_none
import kotlinx.android.synthetic.ethiopia.fragment_receipt.drug_and_supply_edit
import kotlinx.android.synthetic.ethiopia.fragment_receipt.drug_and_supply_items_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.drug_and_supply_line_divider
import kotlinx.android.synthetic.ethiopia.fragment_receipt.drug_and_supply_none
import kotlinx.android.synthetic.ethiopia.fragment_receipt.finish_button
import kotlinx.android.synthetic.ethiopia.fragment_receipt.gender_and_age
import kotlinx.android.synthetic.ethiopia.fragment_receipt.lab_edit
import kotlinx.android.synthetic.ethiopia.fragment_receipt.lab_items_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.lab_line_divider
import kotlinx.android.synthetic.ethiopia.fragment_receipt.lab_none
import kotlinx.android.synthetic.ethiopia.fragment_receipt.medical_record_number
import kotlinx.android.synthetic.ethiopia.fragment_receipt.membership_number
import kotlinx.android.synthetic.ethiopia.fragment_receipt.patient_outcome_value
import kotlinx.android.synthetic.ethiopia.fragment_receipt.provider_comment_date
import kotlinx.android.synthetic.ethiopia.fragment_receipt.provider_comment_text
import kotlinx.android.synthetic.ethiopia.fragment_receipt.referral_date
import kotlinx.android.synthetic.ethiopia.fragment_receipt.referral_reason
import kotlinx.android.synthetic.ethiopia.fragment_receipt.referral_serial_number
import kotlinx.android.synthetic.ethiopia.fragment_receipt.referrals_container
import kotlinx.android.synthetic.ethiopia.fragment_receipt.referring_to
import kotlinx.android.synthetic.ethiopia.fragment_receipt.service_edit
import kotlinx.android.synthetic.ethiopia.fragment_receipt.service_items_list
import kotlinx.android.synthetic.ethiopia.fragment_receipt.service_line_divider
import kotlinx.android.synthetic.ethiopia.fragment_receipt.service_none
import kotlinx.android.synthetic.ethiopia.fragment_receipt.total_price
import kotlinx.android.synthetic.ethiopia.fragment_receipt.visit_type
import kotlinx.android.synthetic.ethiopia.fragment_receipt.visit_type_edit
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.temporal.ChronoUnit
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Encounter.EncounterAction
import org.watsi.domain.entities.Referral
import org.watsi.domain.usecases.DeletePendingClaimAndMemberUseCase
import org.watsi.domain.usecases.LoadOnePendingClaimUseCase
import org.watsi.domain.usecases.LoadOneReturnedClaimUseCase
import org.watsi.uhp.R
import org.watsi.uhp.R.plurals.comment_age
import org.watsi.uhp.R.string.none
import org.watsi.uhp.R.string.today
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.adapters.ReceiptListItemAdapter
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.EnumHelper
import org.watsi.uhp.helpers.EthiopianDateHelper
import org.watsi.uhp.helpers.RecyclerViewHelper
import org.watsi.uhp.helpers.SnackbarHelper
import org.watsi.uhp.helpers.StringHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.utils.CurrencyUtil
import org.watsi.uhp.viewmodels.ReceiptViewModel
import org.watsi.uhp.views.CustomFocusEditText
import javax.inject.Inject

class ReceiptFragment : DaggerFragment() {

    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var deletePendingClaimAndMemberUseCase: DeletePendingClaimAndMemberUseCase
    @Inject lateinit var loadOneReturnedClaimUseCase: LoadOneReturnedClaimUseCase
    @Inject lateinit var loadOnePendingClaimUseCase: LoadOnePendingClaimUseCase
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock

    lateinit var viewModel: ReceiptViewModel
    lateinit var serviceReceiptItemAdapter: ReceiptListItemAdapter
    lateinit var labReceiptItemAdapter: ReceiptListItemAdapter
    lateinit var drugAndSupplyReceiptItemAdapter: ReceiptListItemAdapter
    lateinit var encounterFlowState: EncounterFlowState
    lateinit var encounterAction: EncounterAction

    private var snackbarMessageToShow: String? = null

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
        encounterAction = when {
            encounterFlowState.encounter.adjudicationState == Encounter.AdjudicationState.RETURNED -> EncounterAction.RESUBMIT
            encounterFlowState.encounter.preparedAt == null -> EncounterAction.PREPARE
            encounterFlowState.encounter.submittedAt == null -> EncounterAction.SUBMIT
            else -> throw IllegalStateException("EncounterAction for ReceiptFragment cannot be determined.")
        }

        viewModel = ViewModelProviders.of(this, viewModelFactory).get(ReceiptViewModel::class.java)
        setAndObserveViewModel()

        val services = encounterFlowState.getEncounterItemsOfType(Billable.Type.SERVICE)
        val labs = encounterFlowState.getEncounterItemsOfType(Billable.Type.LAB)
        val drugsAndSupplies = encounterFlowState.getEncounterItemsOfType(Billable.Type.SUPPLY)
                .plus(encounterFlowState.getEncounterItemsOfType(Billable.Type.DRUG))

        serviceReceiptItemAdapter = ReceiptListItemAdapter(services)
        labReceiptItemAdapter = ReceiptListItemAdapter(labs)
        drugAndSupplyReceiptItemAdapter = ReceiptListItemAdapter(drugsAndSupplies)
    }

    private fun setAndObserveViewModel() {
        val editableComment = when (encounterAction) {
            EncounterAction.PREPARE -> encounterFlowState.newProviderComment
            EncounterAction.SUBMIT -> encounterFlowState.encounter.providerComment
            EncounterAction.RESUBMIT -> encounterFlowState.newProviderComment
        }

        viewModel.getObservable(
            encounterFlowState.encounter.occurredAt,
            encounterFlowState.encounter.backdatedOccurredAt,
            editableComment
        ).observe(this, Observer { it?.let { viewState ->
            if (viewState.comment == null) {
                comment_text.setText(none)
            } else {
                comment_text.text = viewState.comment
            }
        } })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(context.getString(R.string.receipt_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        return inflater?.inflate(R.layout.fragment_receipt, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        val genderAndAgeText = StringHelper.formatAgeAndGender(encounterFlowState.member, context, clock)

        if (encounterAction == EncounterAction.SUBMIT) {
            displayPreparedClaimInfo()
        } else if (encounterAction == EncounterAction.RESUBMIT) {
            displayReturnedClaimInfo()
        } else {
            claim_id.text = encounterFlowState.encounter.shortenedClaimId()
        }

        membership_number.text = encounterFlowState.member.membershipNumber
        gender_and_age.text = genderAndAgeText
        medical_record_number.text = encounterFlowState.member.medicalRecordNumber
        visit_type.text = encounterFlowState.encounter.visitType ?: getString(R.string.none)
        total_price.text = getString(R.string.price, CurrencyUtil.formatMoneyWithCurrency(context, encounterFlowState.price()))

        encounterFlowState.referral?.let { referral ->
            referrals_container.visibility = View.VISIBLE
            referral_date.text = EthiopianDateHelper.formatAsEthiopianDate(LocalDate.now(clock))
            referring_to.text = referral.receivingFacility
            referral_serial_number.text = referral.number ?: getString(R.string.none)
            referral_reason.text = EnumHelper.referralReasonToDisplayedString(referral.reason, context, logger)
        }

        encounterFlowState.encounter.patientOutcome.let { patientOutcome ->
            if (patientOutcome != null) {
                patient_outcome_value.text = EnumHelper.patientOutcomeToDisplayedString(patientOutcome, context, logger)
            } else {
                patient_outcome_value.text = getString(R.string.none)
            }
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

        if (encounterFlowState.diagnoses.isEmpty()) {
            diagnoses_none.visibility = View.VISIBLE
            diagnoses_list.visibility = View.GONE
        } else {
            diagnoses_none.visibility = View.GONE
            diagnoses_list.visibility = View.VISIBLE
            diagnoses_list.text = encounterFlowState.diagnoses.joinToString(", ") { it.description }
        }

        comment_container.setOnClickListener {
            launchAddCommentDialog()
        }

        visit_type_edit.setOnClickListener {
            navigationManager.goTo(
                VisitTypeFragment.forEncounter(encounterFlowState)
            )
        }

        diagnoses_edit.setOnClickListener {
            navigationManager.goTo(
                DiagnosisFragment.forEncounter(encounterFlowState)
            )
        }

        service_edit.setOnClickListener {
            navigationManager.goTo(
                SpinnerLineItemFragment.forEncounter(Billable.Type.SERVICE, encounterFlowState)
            )
        }

        lab_edit.setOnClickListener {
            navigationManager.goTo(
                SpinnerLineItemFragment.forEncounter(Billable.Type.LAB, encounterFlowState)
            )
        }

        drug_and_supply_edit.setOnClickListener {
            navigationManager.goTo(
                DrugAndSupplyFragment.forEncounter(encounterFlowState)
            )
        }

        val occurredAtGregorianDate = LocalDateTime.ofInstant(
            encounterFlowState.encounter.occurredAt,
            clock.zone
        ).toLocalDate()

        date_container.setUp(
            initialGregorianValue = occurredAtGregorianDate,
            clock = clock,
            onDateSelected = { dateOfService ->
                viewModel.updateBackdatedOccurredAt(dateOfService.atStartOfDay(clock.zone).toInstant(), encounterFlowState)
            }
        )

        when (encounterAction) {
            EncounterAction.PREPARE -> {
                finish_button.text = getString(R.string.save)
            }
            EncounterAction.SUBMIT -> {
                finish_button.setCompoundDrawablesWithIntrinsicBounds(
                        context.getDrawable(R.drawable.ic_send_white_24dp), null, null, null)
                finish_button.text = getString(R.string.submit_encounter_button)
            }
            EncounterAction.RESUBMIT -> {
                finish_button.setCompoundDrawablesWithIntrinsicBounds(
                        context.getDrawable(R.drawable.ic_return_white_24dp), null, null, null)
                finish_button.text = getString(R.string.resubmit)
            }
        }

        finish_button.setOnClickListener {
            finishEncounter()
        }

        snackbarMessageToShow?.let { snackbarMessage ->
            SnackbarHelper.show(finish_button, context, snackbarMessage)
            snackbarMessageToShow = null
        }
    }

    private fun displayPreparedClaimInfo() {
        claim_id_container.visibility = View.VISIBLE
        claim_id.text = encounterFlowState.encounter.shortenedClaimId()
    }

    private fun displayReturnedClaimInfo() {
        claim_id_container.visibility = View.VISIBLE
        claim_id.text = encounterFlowState.encounter.shortenedClaimId()

        adjudication_comments_container.visibility = View.VISIBLE
        encounterFlowState.encounter.submittedAt?.let {
            val providerCommentDaysAgo = ChronoUnit.DAYS.between(it, Instant.now()).toInt()
            provider_comment_date.text = if (providerCommentDaysAgo > 0) {
                resources.getQuantityString(
                    comment_age, providerCommentDaysAgo, providerCommentDaysAgo
                )
            } else {
                getString(today)
            }

        }
        encounterFlowState.encounter.adjudicatedAt?.let {
            val branchCommentDaysAgo = ChronoUnit.DAYS.between(it, Instant.now()).toInt()
            branch_comment_date.text = if (branchCommentDaysAgo > 0) {
                resources.getQuantityString(
                    comment_age, branchCommentDaysAgo, branchCommentDaysAgo
                )
            } else {
                getString(today)
            }
        }
        provider_comment_text.text = if (encounterFlowState.encounter.providerComment != null) {
            encounterFlowState.encounter.providerComment
        } else {
            getString(none)
        }
        branch_comment_text.text = if (encounterFlowState.encounter.adjudicationReason != null) {
            encounterFlowState.encounter.adjudicationReason
        } else {
            getString(none)
        }
    }

    private fun launchAddCommentDialog() {
        val commentDialogView = LayoutInflater.from(context).inflate(R.layout.dialog_comment, null)
        val editText = commentDialogView.findViewById<View>(R.id.comment_dialog_input) as CustomFocusEditText
        viewModel.comment()?.let { comment ->
            editText.text = Editable.Factory.getInstance().newEditable(comment)
        }

        AlertDialog.Builder(activity)
            .setView(commentDialogView)
            .setNegativeButton(R.string.dialog_cancel, null)
            .setPositiveButton(R.string.dialog_save) { _, _ ->
                viewModel.updateComment(editText.text.toString(), encounterFlowState)
            }.create().show()
    }

    private fun finishEncounter() {
        if ((encounterFlowState.referral == null || encounterFlowState.referral?.reason == Referral.Reason.FOLLOW_UP)
                && encounterFlowState.encounterItemRelations.isEmpty()) {
            SnackbarHelper.showError(finish_button, context, getString(R.string.empty_line_items_warning))
            return
        }

        if (encounterAction == EncounterAction.RESUBMIT && encounterFlowState.newProviderComment.isNullOrBlank()) {
            SnackbarHelper.showError(finish_button, context, getString(R.string.empty_comment_warning))
            return
        }

        if (encounterFlowState.encounter.visitType == null) {
            SnackbarHelper.showError(finish_button, context, getString(R.string.empty_visit_type))
            return
        }

        val message = when (encounterAction) {
            EncounterAction.PREPARE -> {
                getString(R.string.encounter_saved)
            }
            EncounterAction.SUBMIT -> {
                String.format(
                    getString(R.string.claim_id_submitted),
                    encounterFlowState.encounter.shortenedClaimId()
                )
            }
            EncounterAction.RESUBMIT -> {
                String.format(
                    getString(R.string.claim_id_submitted),
                    encounterFlowState.encounter.shortenedClaimId()
                )
            }
        }

        viewModel.finishEncounter(encounterFlowState, encounterAction).subscribe({
            navigateToNext(message)
        }, {
            logger.error(it)
        })
    }

    private fun deleteEncounter() {
        deletePendingClaimAndMemberUseCase.execute(
            encounterId = encounterFlowState.encounter.id
        ).subscribe({
            navigateToNext(
                String.format(
                    getString(R.string.claim_id_deleted),
                    encounterFlowState.encounter.shortenedClaimId()
                )
            )
        }, {
            logger.error(it)
        })
    }

    private fun navigateToNext(message: String) {
        when (encounterAction) {
            EncounterAction.PREPARE -> {
                navigationManager.popTo(HomeFragment.withSnackbarMessage(message))
            }
            EncounterAction.SUBMIT -> {
                // doOnEvent contains the side-effect that executes whenever the Maybe completes, errors, or succeeds.
                // In this case, the side-effect is to navigate to the PendingClaimsFragment.
                // If the Maybe succeeds, we will then navigate to the ReceiptFragment.
                // doOnEvent always happens before any of the subscribe callbacks.
                loadOnePendingClaimUseCase.execute().doOnEvent { _, _ ->
                    navigationManager.popTo(PendingClaimsFragment.withSnackbarMessage(message))
                }.subscribe( { encounterWithExtras->
                    val encounterFlowState = EncounterFlowState.fromEncounterWithExtras(encounterWithExtras)
                    navigationManager.goTo(ReceiptFragment.forEncounter(encounterFlowState))
                }, { error ->
                    logger.warning(error)
                })
            }
            EncounterAction.RESUBMIT -> {
                // See the `EncounterAction.SUBMIT` comment to understand how doOnEvent works with subscribe.
                loadOneReturnedClaimUseCase.execute().doOnEvent { _, _ ->
                    navigationManager.popTo(ReturnedClaimsFragment.withSnackbarMessage(message))
                }.subscribe( { encounterWithExtras->
                    val encounterFlowState = EncounterFlowState.fromEncounterWithExtras(encounterWithExtras)
                    navigationManager.goTo(ReceiptFragment.forEncounter(encounterFlowState))
                }, { error ->
                    logger.warning(error)
                })
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setAndObserveViewModel()
    }

    override fun onPrepareOptionsMenu(menu: Menu?) {
        if (encounterAction == EncounterAction.SUBMIT) {
            menu?.let {
                it.findItem(R.id.menu_delete_claim).isVisible = true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId) {
            R.id.menu_delete_claim -> {
                AlertDialog.Builder(activity)
                    .setTitle(getString(R.string.delete_claim_confirmation))
                    .setNegativeButton(R.string.cancel, null)
                    .setPositiveButton(R.string.delete, { _, _ ->
                        deleteEncounter()
                    }).create().show()
                true
            }
            android.R.id.home -> {
                navigationManager.goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
