package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.repositories.EncounterRepository

class FetchReturnedClaimsUseCase(
        private val persistReturnedEncountersUseCase: PersistReturnedEncountersUseCase,
        private val markReturnedEncounterAsRevisedUseCase: MarkReturnedEncounterAsRevisedUseCase,
        private val encounterRepository: EncounterRepository
) {
    fun execute(): Completable {
        return Completable.fromAction {
            val returnedEncounters = encounterRepository.fetchReturnedClaims().blockingGet()
            // I am assuming this use case
            // - persists edits to existing encounters on fields related to adjudication (i.e. adjudication state, adjudicated at, return reason)
            // - persists new encounter with extras (i.e. encounter, member, encounter items).
            persistReturnedEncountersUseCase.execute(returnedEncounters).blockingAwait()

            // This code is to handle the use case where returned encounters on device
            // need to be marked as revised because they have been revised on another device.
            val returnedClaimIds = returnedEncounters.map { it.encounter.id }
            val clientReturnedEncounterIds = encounterRepository.returnedIds().blockingGet()
            val encountersThatNeedToBeMarkedAsRevised = clientReturnedEncounterIds - returnedClaimIds
            markReturnedEncounterAsRevisedUseCase.execute(encountersThatNeedToBeMarkedAsRevised).blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
