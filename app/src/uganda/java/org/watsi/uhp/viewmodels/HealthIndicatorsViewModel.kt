package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import org.watsi.uhp.flowstates.EncounterFlowState
import javax.inject.Inject

class HealthIndicatorsViewModel @Inject constructor() : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(): LiveData<ViewState> {
        observable.value = ViewState()
        return observable
    }

    fun setHasFever(hasFever: Boolean) {
        observable.value = observable.value?.copy(hasFever = hasFever)
    }

    fun updateEncounterWithHealthIndicators(encounterFlowState: EncounterFlowState) {
        val hasFever = observable.value?.hasFever
        encounterFlowState.encounter = encounterFlowState.encounter.copy(hasFever = hasFever)
    }

    data class ViewState(val hasFever: Boolean? = null)
}
