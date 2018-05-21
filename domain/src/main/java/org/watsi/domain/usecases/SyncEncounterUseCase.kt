package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository

class SyncEncounterUseCase(
        private val encounterRepository: EncounterRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(): Completable {
        return deltaRepository.unsyncedModelIds(Delta.ModelName.IDENTIFICATION_EVENT, Delta.Action.ADD).flatMapCompletable {
            unsyncedIdEventIds ->
            deltaRepository.unsynced(Delta.ModelName.ENCOUNTER).flatMapCompletable { encounterDeltas ->
                Completable.concat(encounterDeltas.map { encounterDelta ->
                    encounterRepository.find(encounterDelta.modelId).flatMapCompletable {
                        // filter out deltas that correspond to an IdEvent that has not been synced yet
                        if (!unsyncedIdEventIds.contains(it.encounter.identificationEventId)) {
                            Completable.concat(listOf(
                                    encounterRepository.sync(encounterDelta),
                                    deltaRepository.markAsSynced(listOf(encounterDelta))
                            ))
                        } else {
                            Completable.complete()
                        }
                    }
                })
            }
        }
    }
}
