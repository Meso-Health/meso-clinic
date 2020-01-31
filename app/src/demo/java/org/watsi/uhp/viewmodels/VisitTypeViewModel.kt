package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import org.threeten.bp.Clock
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Referral
import org.watsi.uhp.R
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
import javax.inject.Inject

class VisitTypeViewModel @Inject constructor(val clock: Clock): ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(encounterFlowState: EncounterFlowState): LiveData<ViewState> {
        observable.value = ViewState(
            selectedVisitType = encounterFlowState.encounter.visitType ?: Encounter.VISIT_TYPE_CHOICES[0],
            patientOutcome = encounterFlowState.encounter.patientOutcome,
            isReferralOrFollowUp = encounterFlowState.referral != null,
            receivingFacility = encounterFlowState.referral?.receivingFacility,
            reason = encounterFlowState.referral?.reason,
            number = encounterFlowState.referral?.number,
            referralDate = encounterFlowState.referral?.date ?: LocalDate.now(clock),
            validationErrors = emptyMap()
        )
        return observable
    }

    fun onSelectVisitType(visitType: String) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(selectedVisitType = visitType)
        }
    }

    fun onNumberChange(number: String?) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(number = number)
        }
    }

    fun onReasonChange(reason: Referral.Reason?) {
        observable.value?.let { viewState ->
            val validationErrors = viewState.validationErrors.filterNot { it.key == REASON_ERROR }
            observable.value = viewState.copy(reason = reason, validationErrors = validationErrors)
        }
    }

    fun onReceivingFacilityChange(receivingFacility: String?) {
        observable.value?.let { viewState ->
            val validationErrors = viewState.validationErrors.filterNot { it.key == RECEIVING_FACILITY_ERROR }
            observable.value = viewState.copy(receivingFacility = receivingFacility, validationErrors = validationErrors)
        }
    }

    fun onUpdateReferralDate(referralDate: LocalDate) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(referralDate = referralDate)
        }
    }

    fun onUpdatePatientOutcome(patientOutcome: Encounter.PatientOutcome?) {
        observable.value?.let { viewState ->
            val isReferralOrFollowUp = (patientOutcome == Encounter.PatientOutcome.REFERRED) ||
                    (patientOutcome == Encounter.PatientOutcome.FOLLOW_UP)
            observable.value = viewState.copy(
                patientOutcome = patientOutcome,
                isReferralOrFollowUp = isReferralOrFollowUp
            )
        }
    }

    object FormValidator {
        fun validateViewState(viewState: ViewState): Map<String, Int> {
            val errors = HashMap<String, Int>()
            if (viewState.patientOutcome == Encounter.PatientOutcome.REFERRED) {
                if (viewState.reason == null) {
                    errors[REASON_ERROR] = R.string.referral_reason_validation_error
                }

                if (viewState.receivingFacility.isNullOrBlank()) {
                    errors[RECEIVING_FACILITY_ERROR] = R.string.receiving_facility_validation_error
                }
            }
            return errors
        }
    }

    companion object {
        const val REASON_ERROR = "referral_reason_error"
        const val RECEIVING_FACILITY_ERROR = "referral_receiving_facility_error"
    }

    fun validateAndUpdateEncounterFlowState(encounterFlowState: EncounterFlowState): Completable {
        return observable.value?.let { viewState ->
            val validationErrors = FormValidator.validateViewState(viewState)
            if (validationErrors.isNotEmpty()) {
                observable.value = viewState.copy(validationErrors = validationErrors)
                Completable.error(ValidationException("Some required referral fields are missing", validationErrors))
            } else {
                Completable.fromAction {
                    encounterFlowState.encounter = encounterFlowState.encounter.copy(
                        visitType = viewState.selectedVisitType,
                        patientOutcome = viewState.patientOutcome
                    )
                    if (viewState.isReferralOrFollowUp) {
                        val isFollowUp = viewState.patientOutcome == Encounter.PatientOutcome.FOLLOW_UP
                        val receivingFacility = if (isFollowUp) "SELF" else viewState.receivingFacility!!
                        val referralReason = if (isFollowUp) Referral.Reason.FOLLOW_UP else viewState.reason!!
                        val number = if (isFollowUp) null else viewState.number

                        encounterFlowState.referral = Referral(
                            id = UUID.randomUUID(),
                            receivingFacility = receivingFacility,
                            reason = referralReason,
                            number = number,
                            encounterId = encounterFlowState.encounter.id,
                            date = viewState.referralDate
                        )
                    } else {
                        encounterFlowState.referral = null
                    }
                }
            }
        } ?: Completable.never()
    }

    data class ValidationException(val msg: String, val errors: Map<String, Int>): Exception(msg)

    data class ViewState(
        val selectedVisitType: String,
        val patientOutcome: Encounter.PatientOutcome?,
        val isReferralOrFollowUp: Boolean,
        val referralDate: LocalDate,
        val receivingFacility: String?,
        val reason: Referral.Reason?,
        val number: String?,
        val validationErrors: Map<String, Int>
    )
}
