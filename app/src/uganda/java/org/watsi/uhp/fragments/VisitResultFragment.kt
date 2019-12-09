package org.watsi.uhp.fragments

import android.arch.lifecycle.Observer
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.uganda.fragment_visit_result.done_button
import kotlinx.android.synthetic.uganda.fragment_visit_result.patient_outcome_spinner
import kotlinx.android.synthetic.uganda.fragment_visit_result.referral_reason_container
import kotlinx.android.synthetic.uganda.fragment_visit_result.referral_reason_spinner
import org.threeten.bp.Clock
import org.watsi.device.managers.Logger
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.EnumHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.VisitResultViewModel
import javax.inject.Inject

class VisitResultFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock
    lateinit var encounterFlowState: EncounterFlowState
    lateinit var viewModel: VisitResultViewModel

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterFlowState): VisitResultFragment {
            val fragment = VisitResultFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VisitResultViewModel::class.java)
        viewModel.getObservable(encounterFlowState).observe(this, Observer {
            it?.let { viewState ->
                viewState.validationErrors[VisitResultViewModel.REASON_ERROR].let { errorResourceId ->
                    referral_reason_spinner.setError(errorResourceId?.let { getString(it) })
                }
                if (viewState.isReferral) {
                    referral_reason_container.visibility = View.VISIBLE
                } else {
                    referral_reason_container.visibility = View.GONE
                }
            }
        })
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(getString(R.string.visit_result_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        (activity as ClinicActivity).setSoftInputModeToPan()
        return inflater?.inflate(R.layout.fragment_visit_result, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        setUpSpinners()

        done_button.setOnClickListener {
            viewModel.validateAndUpdateEncounterFlowState(encounterFlowState).subscribe({
                navigationManager.goTo(ReceiptFragment.forEncounter(encounterFlowState))
            }, { throwable ->
                if (throwable is VisitResultViewModel.ValidationException) {
                    // do nothing for now. No need to say "some fields are invalid"
                } else {
                    logger.error(throwable)
                }
            })
        }
    }

    fun setUpSpinners() {
        val patientOutcomeMappings = EnumHelper.getPatientOutcomeMappings()
        val patientOutcomeEnums = patientOutcomeMappings.map { it.first }
        val patientOutcomeStrings = patientOutcomeMappings.map { getString(it.second) }
        val initialPatientOutcome = patientOutcomeMappings.find {
            it.first == encounterFlowState.encounter.patientOutcome
        }?.let { context.getString(it.second) }

        patient_outcome_spinner.setUpWithPrompt(
            choices = patientOutcomeStrings,
            initialChoice = initialPatientOutcome,
            onItemSelected = { index: Int -> viewModel.onUpdatePatientOutcome(patientOutcomeEnums[index]) },
            promptString = getString(R.string.patient_outcome_prompt),
            onPromptSelected = { viewModel.onUpdatePatientOutcome(null) }
        )

        val referralReasonMappings = EnumHelper.getReferralReasonMappings()
        val referralReasonEnums = referralReasonMappings.map { it.first }
        val referralReasonStrings = referralReasonMappings.map { getString(it.second) }
        val initialReferralReason = referralReasonMappings.find {
            it.first == encounterFlowState.referral?.reason
        }?.let { context.getString(it.second) }

        referral_reason_spinner.setUpWithPrompt(
            choices = referralReasonStrings,
            initialChoice = initialReferralReason,
            onItemSelected = { index -> viewModel.onReasonChange(referralReasonEnums[index]) },
            promptString = getString(R.string.referral_reason_prompt),
            onPromptSelected = { viewModel.onReasonChange(null) }
        )
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                navigationManager.goBack()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as ClinicActivity).setSoftInputModeToPan()
    }

    override fun onResume() {
        super.onResume()

        // This is needed in order to fix a weird bug where navigating back to this screen
        // would set the wrong pre-selected options for each dropdown (visit type, referral facility, referral reason)
        setUpSpinners()
    }
}
