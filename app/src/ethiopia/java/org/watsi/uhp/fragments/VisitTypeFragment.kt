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
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.date_container
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.done_button
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.patient_outcome_spinner
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.receiving_facility_container
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.receiving_facility_spinner
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.referral_form
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.referral_reason_container
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.referral_reason_spinner
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.referral_serial_number
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.referral_serial_number_container
import kotlinx.android.synthetic.ethiopia.fragment_visit_type.visit_type_spinner
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Referral
import org.watsi.uhp.R
import org.watsi.uhp.activities.ClinicActivity
import org.watsi.uhp.flowstates.EncounterFlowState
import org.watsi.uhp.helpers.EnumHelper
import org.watsi.uhp.helpers.LayoutHelper
import org.watsi.uhp.managers.NavigationManager
import org.watsi.uhp.viewmodels.VisitTypeViewModel
import org.watsi.uhp.views.SpinnerField
import javax.inject.Inject

class VisitTypeFragment : DaggerFragment() {
    @Inject lateinit var navigationManager: NavigationManager
    @Inject lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject lateinit var logger: Logger
    @Inject lateinit var clock: Clock
    lateinit var encounterFlowState: EncounterFlowState
    lateinit var viewModel: VisitTypeViewModel

    companion object {
        const val PARAM_ENCOUNTER = "encounter"

        fun forEncounter(encounter: EncounterFlowState): VisitTypeFragment {
            val fragment = VisitTypeFragment()
            fragment.arguments = Bundle().apply {
                putSerializable(PARAM_ENCOUNTER, encounter)
            }
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        encounterFlowState = arguments.getSerializable(PARAM_ENCOUNTER) as EncounterFlowState
        viewModel = ViewModelProviders.of(this, viewModelFactory).get(VisitTypeViewModel::class.java)
        viewModel.getObservable(encounterFlowState).observe(this, Observer {
            it?.let { viewState ->
                setErrors(viewState.validationErrors)

                if (viewState.isReferralOrFollowUp) {
                    referral_form.visibility = View.VISIBLE
                    if (viewState.patientOutcome == Encounter.PatientOutcome.REFERRED) {
                        date_container.setLabel(getString(R.string.referral_date_label))
                        receiving_facility_container.visibility = View.VISIBLE
                        referral_reason_container.visibility = View.VISIBLE
                        referral_serial_number_container.visibility = View.VISIBLE
                    } else {
                        date_container.setLabel(getString(R.string.follow_up_date_label))
                        receiving_facility_container.visibility = View.GONE
                        referral_reason_container.visibility = View.GONE
                        referral_serial_number_container.visibility = View.GONE
                    }
                } else {
                    referral_form.visibility = View.GONE
                }
            }
        })
    }

    private fun setErrors(errors: Map<String, Int>) {
        errors[VisitTypeViewModel.REASON_ERROR].let { errorResourceId ->
            referral_reason_spinner.setError(errorResourceId?.let { getString(it) })
        }

        errors[VisitTypeViewModel.RECEIVING_FACILITY_ERROR].let { errorResourceId ->
            receiving_facility_spinner.setError(errorResourceId?.let { getString(it) })
        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        (activity as ClinicActivity).setToolbar(getString(R.string.visit_type_fragment_label), R.drawable.ic_arrow_back_white_24dp)
        setHasOptionsMenu(true)
        (activity as ClinicActivity).setSoftInputModeToPan()
        return inflater?.inflate(R.layout.fragment_visit_type, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        setUpSpinners()

        referral_serial_number.addTextChangedListener(LayoutHelper.OnChangedListener {
            number -> viewModel.onNumberChange(number)
        })

        done_button.setOnClickListener {
            viewModel.validateAndUpdateEncounterFlowState(encounterFlowState).subscribe({
                navigationManager.popTo(ReceiptFragment.forEncounter(encounterFlowState))
            }, { throwable ->
                if (throwable is VisitTypeViewModel.ValidationException) {
                    // do nothing for now. No need to say "some fields are invalid"
                } else {
                    logger.error(throwable)
                }
            })
        }

        date_container.setUp(
            initialGregorianValue = LocalDate.now(clock),
            clock = clock,
            onDateSelected = { dateOfReferral -> viewModel.onUpdateReferralDate(dateOfReferral) }
        )

        encounterFlowState.referral?.number?.let { number ->
            referral_serial_number.setText(number)
        }
    }

    fun setUpSpinners() {
        val visitTypes = Encounter.VISIT_TYPE_CHOICES

        visit_type_spinner.setUpWithoutPrompt(
            adapter = SpinnerField.createAdapter(context, visitTypes),
            initialChoiceIndex = visitTypes.indexOf(encounterFlowState.encounter.visitType ?: visitTypes[0]),
            onItemSelected = { selectedVisitType : String ->
                viewModel.onSelectVisitType(selectedVisitType)
            }
        )

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

        receiving_facility_spinner.setUpWithPrompt(
            choices = Referral.RECEIVING_FACILITY_CHOICES,
            initialChoice = encounterFlowState.referral?.receivingFacility,
            onItemSelected = { index ->
                viewModel.onReceivingFacilityChange(Referral.RECEIVING_FACILITY_CHOICES[index])
            },
            promptString = getString(R.string.referred_to_facility_prompt),
            onPromptSelected = { viewModel.onReceivingFacilityChange(null) },
            otherChoicesHint = context.getString(R.string.referral_other_option),
            onOtherChoicesTextChange = { customFacility ->
                viewModel.onReceivingFacilityChange(customFacility)
            }
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
