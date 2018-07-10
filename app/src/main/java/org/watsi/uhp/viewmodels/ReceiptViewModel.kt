package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Instant
import org.watsi.domain.relations.EncounterBuilder
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import javax.inject.Inject

class ReceiptViewModel @Inject constructor(
    private val createEncounterUseCase: CreateEncounterUseCase,
    private val createMemberUseCase: CreateMemberUseCase
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(occurredAt: Instant, backdatedOccurredAt: Boolean): LiveData<ViewState> {
        observable.value = ViewState(occurredAt, backdatedOccurredAt)
        return observable
    }

    fun updateBackdatedOccurredAt(instant: Instant) {
        observable.value = observable.value?.copy(occurredAt = instant, backdatedOccurredAt = true)
    }

    fun updateEncounterWithDate(encounterBuilder: EncounterBuilder) {
        observable.value?.let { viewState ->
            encounterBuilder.encounter = encounterBuilder.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt
            )
        }
    }

    fun submitEncounter(
            encounterBuilder: EncounterBuilder,
            copaymentPaid: Boolean
    ): Completable {
        return observable.value?.let { viewState ->
            encounterBuilder.encounter = encounterBuilder.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt,
                copaymentPaid = copaymentPaid
            )
            Completable.fromCallable {
                createEncounterUseCase.execute(encounterBuilder.toEncounterWithItemsAndForms()).blockingAwait()
                encounterBuilder.member?.let { createMemberUseCase.execute(it).blockingAwait() }
            }.observeOn(AndroidSchedulers.mainThread())
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
