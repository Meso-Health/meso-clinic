package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Encounter
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.CreateMemberUseCase
import org.watsi.domain.usecases.ReviseMemberAndClaimUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import javax.inject.Inject

class ReceiptViewModel @Inject constructor(
    private val createEncounterUseCase: CreateEncounterUseCase,
    private val createMemberUseCase: CreateMemberUseCase,
    private val reviseMemberAndClaimUseCase: ReviseMemberAndClaimUseCase
) : ViewModel() {

    @Inject lateinit var logger: Logger

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(occurredAt: Instant, backdatedOccurredAt: Boolean, comment: String?): LiveData<ViewState> {
        observable.value = ViewState(occurredAt, backdatedOccurredAt, comment)
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

    fun updateComment(comment: String) {
        observable.value = observable.value?.copy(comment = comment)
    }

    fun submitEncounter(
        encounterFlowState: EncounterFlowState
    ): Completable {
        return observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt,
                providerComment = viewState.comment,
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
                            reviseMemberAndClaimUseCase.execute(it, encounterFlowState.toEncounterWithItemsAndForms()).blockingAwait()
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

    fun comment(): String? {
        return observable.value?.comment
    }

    data class ViewState(val occurredAt: Instant,
                         val backdatedOccurredAt: Boolean,
                         val comment: String? = null)
}
