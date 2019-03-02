package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.Referral
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
import javax.inject.Inject

class VisitTypeViewModel @Inject constructor(): ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(encounterFlowState: EncounterFlowState): LiveData<ViewState> {
        observable.value = ViewState(
            selectedVisitType = encounterFlowState.encounter.visitType ?: Encounter.VISIT_TYPE_CHOICES[0]
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
            observable.value = viewState.copy(reason = reason)
        }
    }

    fun onReceivingFacilityChange(receivingFacility: String?) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(receivingFacility = receivingFacility)
        }
    }

    object FormValidator {
        fun validateViewState(viewState: ViewState): Map<String, Int> {
            val errors = HashMap<String, Int>()
            // TODO: Actually code up the errors
            return errors
        }
    }

    fun validateAndUpdateEncounterFlowState(encounterFlowState: EncounterFlowState) {
        observable.value?.let { viewState ->
            val validationErrors = FormValidator.validateViewState(viewState)
            if (validationErrors.isNotEmpty()) {
                // Placeholder for now.
            } else {
                encounterFlowState.encounter = encounterFlowState.encounter.copy(visitType = viewState.selectedVisitType)
                if (viewState.referralBoxChecked && viewState.receivingFacility != null && viewState.reason != null) {
                    encounterFlowState.referrals = listOf(Referral(
                        id = UUID.randomUUID(),
                        receivingFacility = viewState.receivingFacility,
                        reason = viewState.reason,
                        number = viewState.number,
                        encounterId = encounterFlowState.encounter.id
                    ))
                } else {
                    encounterFlowState.referrals = emptyList()
                }
            }
        }
        // TODO: Figure out what to di if viewState is null
    }

    data class ViewState(
        val selectedVisitType: String,
        val referralBoxChecked: Boolean = false,
        val receivingFacility: String? = null,
        val reason: String? = null,
        val number: String? = null,
        val validationErrors: Map<String, Int> = emptyMap() // TODO: Will use this later
    )
}
