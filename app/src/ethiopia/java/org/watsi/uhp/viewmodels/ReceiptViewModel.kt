package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
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

    @Inject lateinit var logger: Logger

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
        encounterFlowState: EncounterFlowState
    ): Completable {
        return observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt,
                copaymentPaid = null    // no copayment in ethiopia system
            )
            Completable.fromCallable {
                if (encounterFlowState.member == null) {
                    logger.error("Member cannot be null")
                }

                encounterFlowState.member?.let {
                    when (encounterFlowState.encounter.adjudicationState) {
                        Encounter.AdjudicationState.PENDING -> {
                            createMemberUseCase.execute(it).blockingAwait()
                            createEncounterUseCase.execute(encounterFlowState.toEncounterWithItemsAndForms()).blockingAwait()
                        }
                        Encounter.AdjudicationState.RETURNED -> {
                            // TODO: move this logic into it's own usecase (to be done in: https://www.pivotaltracker.com/story/show/159841813)

                            // create new member:
                            val newMemberId = UUID.randomUUID()
                            val newMember = it.copy(id = newMemberId)

                            createMemberUseCase.execute(newMember).blockingAwait()

                            // create new encounter with new member and set old encounter ID on revisedEncounterId:
                            val oldEncounterId = encounterFlowState.encounter.id
                            val newEncounter = encounterFlowState.encounter.copy(id = UUID.randomUUID(), memberId = newMemberId, revisedEncounterId = oldEncounterId)

                            // create new encounter items:
                            val newEncounterItems = encounterFlowState.encounterItems.map {
                                EncounterItemWithBillable(it.encounterItem.copy(id = UUID.randomUUID()), it.billable)
                            }

                            createEncounterUseCase.execute(EncounterWithItemsAndForms(newEncounter, newEncounterItems, emptyList(), encounterFlowState.diagnoses)).blockingAwait()
                        }
                        else -> {
                            logger.error("Adjudication state must be PENDING or RETURNED")
                        }
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
