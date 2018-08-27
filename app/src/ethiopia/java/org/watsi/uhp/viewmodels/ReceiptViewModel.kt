package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import java.util.UUID
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

    fun updateEncounterWithDate(encounterFlowState: EncounterFlowState) {
        observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt
            )
        }
    }

    fun submitEncounter(
        encounterFlowState: EncounterFlowState,
        copaymentPaid: Boolean? = null
    ): Completable {
        return observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt,
                copaymentPaid = copaymentPaid
            )
            Completable.fromCallable {
                if (encounterFlowState.member == null) {
                    throw IllegalStateException("Member cannot be null")
                }

                encounterFlowState.member?.let {
                    if (encounterFlowState.encounter.adjudicationState == Encounter.AdjudicationState.PENDING) {
                        createMemberUseCase.execute(it).blockingAwait()
                        createEncounterUseCase.execute(encounterFlowState.toEncounterWithItemsAndForms()).blockingAwait()
                    } else {
                        // create new member:
                        val newMemberId = UUID.randomUUID()
                        val newMember = it.copy(id = newMemberId)

                        createMemberUseCase.execute(newMember).blockingAwait()

                        // create new encounter with new member:
                        val oldEncounterId = encounterFlowState.encounter.id
                        val newEncounter = encounterFlowState.encounter.copy(id = UUID.randomUUID(), memberId = newMemberId, revisedEncounterId = oldEncounterId)

                        // create new encounter items:
                        val newEncounterItems = encounterFlowState.encounterItems.map {
                            EncounterItemWithBillable(it.encounterItem.copy(id = UUID.randomUUID()), it.billable)
                        }

                        createEncounterUseCase.execute(EncounterWithItemsAndForms(newEncounter, newEncounterItems, emptyList(), encounterFlowState.diagnoses)).blockingAwait()
                    }
                }
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
