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
            referralBoxChecked = encounterFlowState.referral != null,
            receivingFacility = encounterFlowState.referral?.receivingFacility,
            reason = encounterFlowState.referral?.reason,
            number = encounterFlowState.referral?.number,
            referralDate = encounterFlowState.referral?.date ?: LocalDate.now(clock)
        )
        return observable
    }

    fun onSelectVisitType(visitType: String) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(selectedVisitType = visitType)
        }
    }

    fun onToggleReferralCheckBox(isChecked: Boolean) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(referralBoxChecked = isChecked)
        }
    }

    fun onNumberChange(number: String?) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(number = number)
        }
    }

    fun onReasonChange(reason: String?) {
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

    object FormValidator {
        fun validateViewState(viewState: ViewState): Map<String, Int> {
            val errors = HashMap<String, Int>()
            if (viewState.referralBoxChecked) {
                if (viewState.reason.isNullOrBlank()) {
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
                    encounterFlowState.encounter = encounterFlowState.encounter.copy(visitType = viewState.selectedVisitType)
                    if (viewState.referralBoxChecked && viewState.receivingFacility != null && viewState.reason != null) {
                        encounterFlowState.referral = Referral(
                            id = UUID.randomUUID(),
                            receivingFacility = viewState.receivingFacility,
                            reason = viewState.reason,
                            number = viewState.number,
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
        val referralBoxChecked: Boolean = false,
        val referralDate: LocalDate,
        val receivingFacility: String? = null,
        val reason: String? = null,
        val number: String? = null,
        val validationErrors: Map<String, Int> = emptyMap()
    )
}
