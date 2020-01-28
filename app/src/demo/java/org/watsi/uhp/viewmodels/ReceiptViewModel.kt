package org.watsi.uhp.viewmodels

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.ViewModel
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.managers.Logger
import org.watsi.domain.entities.Encounter.EncounterAction
import org.watsi.domain.usecases.CheckForSameDayEncountersUseCase
import org.watsi.domain.usecases.CreateEncounterUseCase
import org.watsi.domain.usecases.ReviseClaimUseCase
import org.watsi.domain.usecases.SubmitClaimUseCase
import org.watsi.domain.usecases.UpdateEncounterUseCase
import org.watsi.uhp.flowstates.EncounterFlowState
import javax.inject.Inject

class ReceiptViewModel @Inject constructor(
    private val createEncounterUseCase: CreateEncounterUseCase,
    private val updateEncounterUseCase: UpdateEncounterUseCase,
    private val submitClaimUseCase: SubmitClaimUseCase,
    private val reviseClaimUseCase: ReviseClaimUseCase,
    private val sameDayEncountersUseCase: CheckForSameDayEncountersUseCase,
    private val logger: Logger,
    private val clock: Clock
) : ViewModel() {

    private val observable = MutableLiveData<ViewState>()

    fun getObservable(occurredAt: Instant, backdatedOccurredAt: Boolean, comment: String?): LiveData<ViewState> {
        observable.value = ViewState(occurredAt, backdatedOccurredAt, comment)
        return observable
    }

    fun updateBackdatedOccurredAt(instant: Instant, encounterFlowState: EncounterFlowState) {
        observable.value = observable.value?.copy(occurredAt = instant, backdatedOccurredAt = true)
        observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt
            )
        }
    }

    fun updateComment(comment: String, encounterFlowState: EncounterFlowState) {
        observable.value = observable.value?.copy(comment = comment)
        encounterFlowState.newProviderComment = comment
    }

    fun finishEncounter(
        encounterFlowState: EncounterFlowState,
        encounterAction: EncounterAction
    ): Completable {
        return observable.value?.let { viewState ->
            encounterFlowState.encounter = encounterFlowState.encounter.copy(
                occurredAt = viewState.occurredAt,
                backdatedOccurredAt =  viewState.backdatedOccurredAt,
                providerComment = viewState.comment,
                copaymentAmount = 0
            )
            Completable.fromCallable {

                when (encounterAction) {
                    EncounterAction.PREPARE -> {
                        val isDuplicate = sameDayEncountersUseCase.execute(encounterFlowState.encounter).blockingGet()
                        if (isDuplicate) {
                            throw CheckForSameDayEncountersUseCase.SameDayEncounterException()
                        } else {
                            createEncounterUseCase.execute(
                                encounterFlowState.toEncounterWithExtras(), false, false, clock
                            ).blockingAwait()
                        }
                    }
                    EncounterAction.SUBMIT -> {
                        Completable.concatArray(
                            updateEncounterUseCase.execute(
                                encounterFlowState.toEncounterWithExtras()
                            ),
                            submitClaimUseCase.execute(
                                encounterFlowState.toEncounterWithExtras(), clock
                            )
                        ).blockingAwait()
                    }
                    EncounterAction.RESUBMIT -> {
                        reviseClaimUseCase.execute(
                            encounterFlowState.toEncounterWithExtras(), clock
                        ).blockingAwait()
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

    // This view state needs to keep track of occurredAt and comment because those fields are
    // currently in use to show the comments section in the top of the claim. If we modify that directly
    // on the encounterFlowState, we end up modifying when / which comments are shown at the top
    // of the receipt view.
    data class ViewState(val occurredAt: Instant,
                         val backdatedOccurredAt: Boolean,
                         val comment: String? = null)
}
