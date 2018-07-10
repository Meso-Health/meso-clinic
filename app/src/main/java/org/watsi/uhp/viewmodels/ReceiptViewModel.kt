package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.relations.MutableEncounterWithItemsAndForms
import org.watsi.domain.usecases.CreateEncounterUseCase
import javax.inject.Inject

class ReceiptViewModel @Inject constructor(
    private val createEncounterUseCase: CreateEncounterUseCase
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(occurredAt: Instant, backdatedOccurredAt: Boolean): LiveData<ViewState> {
        observable.value = ViewState(occurredAt, backdatedOccurredAt)
        return observable
    }

    fun updateBackdatedOccurredAt(instant: Instant) {
        observable.value = observable.value?.copy(occurredAt = instant, backdatedOccurredAt = true)
    }

    fun updateEncounterWithDate(encounterRelation: MutableEncounterWithItemsAndForms) {
        observable.value?.let { viewState ->
            encounterRelation.encounter = encounterRelation.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt
            )
        }

    }

    fun submitEncounter(
        encounterRelation: MutableEncounterWithItemsAndForms,
        copaymentPaid: Boolean
    ): Completable {
        return observable.value?.let { viewState ->
            encounterRelation.encounter = encounterRelation.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt,
                copaymentPaid = copaymentPaid
            )
            createEncounterUseCase.execute(encounterRelation.toEncounterWithItemsAndForms())
                .observeOn(AndroidSchedulers.mainThread())
        } ?: Completable.never()
    }

    fun occurredAt(): Instant? {
        return observable.value?.occurredAt
    }

    fun backdatedOccurredAt(): Boolean? {
        return observable.value?.backdatedOccurredAt
    }

    data class ViewState(val occurredAt: Instant,
                         val backdatedOccurredAt: Boolean)
}
