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
            patientOutcome = encounterFlowState.encounter.patientOutcome
        )
        return observable
    }

    fun onUpdatePatientOutcome(patientOutcome: Encounter.PatientOutcome?) {
        observable.value?.let { viewState ->
            observable.value = viewState.copy(
                patientOutcome = patientOutcome
            )
        }
    }

    fun updateEncounterFlowState(encounterFlowState: EncounterFlowState) {
        observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                patientOutcome = viewState.patientOutcome
            )
        }
    }

    data class ViewState(
        val patientOutcome: Encounter.PatientOutcome?
    )
}
