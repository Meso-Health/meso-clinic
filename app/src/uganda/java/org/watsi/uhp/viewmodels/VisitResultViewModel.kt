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

class VisitResultViewModel @Inject constructor(val clock: Clock): ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(encounterFlowState: EncounterFlowState): LiveData<ViewState> {
        observable.value = ViewState(
            patientOutcome = encounterFlowState.encounter.patientOutcome,
            isReferral = encounterFlowState.referral != null,
            reason = encounterFlowState.referral?.reason,
            validationErrors = emptyMap()
        )
        return observable
    }

    fun onUpdatePatientOutcome(patientOutcome: Encounter.PatientOutcome?) {
        val isReferral = (patientOutcome == Encounter.PatientOutcome.REFERRED)
        observable.value?.let { viewState ->
            val validationErrors = viewState.validationErrors.filterNot { it.key == REASON_ERROR }
            observable.value = viewState.copy(
                patientOutcome = patientOutcome,
                isReferral = isReferral,
                validationErrors = validationErrors
            )
        }
    }

    fun onReasonChange(reason: Referral.Reason?) {
        observable.value?.let { viewState ->
            val validationErrors = viewState.validationErrors.filterNot { it.key == REASON_ERROR }
            observable.value = viewState.copy(reason = reason, validationErrors = validationErrors)
        }
    }

    object FormValidator {
        fun validateViewState(viewState: ViewState): Map<String, Int> {
            val errors = HashMap<String, Int>()
            if (viewState.patientOutcome == Encounter.PatientOutcome.REFERRED) {
                if (viewState.reason == null) {
                    errors[REASON_ERROR] = R.string.referral_reason_validation_error
                }
            }
            return errors
        }
    }

    companion object {
        const val REASON_ERROR = "referral_reason_error"
    }

    fun validateAndUpdateEncounterFlowState(encounterFlowState: EncounterFlowState): Completable {
        return observable.value?.let { viewState ->
            val validationErrors = FormValidator.validateViewState(viewState)
            if (validationErrors.isNotEmpty()) {
                observable.value = viewState.copy(validationErrors = validationErrors)
                Completable.error(ValidationException("Referral reason is missing", validationErrors))
            } else {
                Completable.fromAction {
                    encounterFlowState.encounter = encounterFlowState.encounter.copy(
                        patientOutcome = viewState.patientOutcome
                    )
                    if (viewState.isReferral) {
                        encounterFlowState.referral = Referral(
                            id = UUID.randomUUID(),
                            receivingFacility = Referral.UNSPECIFIED_FACILTY,
                            reason = viewState.reason!!,
                            number = null,
                            encounterId = encounterFlowState.encounter.id,
                            date =  LocalDate.now(clock)
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
        val patientOutcome: Encounter.PatientOutcome?,
        val isReferral: Boolean,
        val reason: Referral.Reason?,
        val validationErrors: Map<String, Int>
    )
}
