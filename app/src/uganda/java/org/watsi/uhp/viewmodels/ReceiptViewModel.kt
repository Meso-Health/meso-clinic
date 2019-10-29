package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import javax.inject.Inject

class ReceiptViewModel @Inject constructor(
    private val createEncounterUseCase: CreateEncounterUseCase
) : ViewModel() {

    @Inject lateinit var clock: Clock

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(occurredAt: Instant, backdatedOccurredAt: Boolean, copaymentAmount: Int): LiveData<ViewState> {
        observable.value = ViewState(occurredAt, backdatedOccurredAt, copaymentAmount)
        return observable
    }

    fun updateBackdatedOccurredAt(instant: Instant) {
        observable.value = observable.value?.copy(occurredAt = instant, backdatedOccurredAt = true)
    }

    fun updateCopaymentAmount(amount: Int?) {
        validateAndUpdate(observable.value?.copy(copaymentAmount = amount))
    }

    fun updateEncounterWithDate(encounterFlowState: EncounterFlowState) {
        observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt
            )
        }
    }

    fun submitEncounter(
        encounterFlowState: EncounterFlowState
    ): Completable {
        return observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt,
                copaymentAmount = if (viewState.copaymentAmount != null) viewState.copaymentAmount else 0
            )
            Completable.fromCallable {
                createEncounterUseCase.execute(encounterFlowState.toEncounterWithExtras(), true, false, clock).blockingAwait()
            }.observeOn(AndroidSchedulers.mainThread())
        } ?: Completable.never()
    }

    fun occurredAt(): Instant? {
        return observable.value?.occurredAt
    }

    fun backdatedOccurredAt(): Boolean? {
        return observable.value?.backdatedOccurredAt
    }

    fun copaymentAmount(): Int? {
        return observable.value?.copaymentAmount
    }

    private fun validateAndUpdate(state: ViewState? = observable.value) {
        val isValid = state?.copaymentAmount != null
        observable.value = state?.copy(isValid = isValid)
    }

    data class ViewState(val occurredAt: Instant,
                         val backdatedOccurredAt: Boolean,
                         val copaymentAmount: Int?,
                         val isValid: Boolean = true)
}
