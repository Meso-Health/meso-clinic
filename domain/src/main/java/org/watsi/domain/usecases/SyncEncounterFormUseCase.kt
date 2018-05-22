package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterFormRepository

class SyncEncounterFormUseCase(
        private val encounterFormRepository: EncounterFormRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(): Completable {
        return deltaRepository.unsyncedModelIds(Delta.ModelName.ENCOUNTER, Delta.Action.ADD).flatMapCompletable {
            unsyncedEncounterIds ->
            deltaRepository.unsynced(Delta.ModelName.ENCOUNTER_FORM).flatMapCompletable { encounterFormDeltas ->
                Completable.concat(encounterFormDeltas.map { encounterFormDelta ->
                    encounterFormRepository.find(encounterFormDelta.modelId).flatMapCompletable {
                        // filter out deltas that correspond to a Encounter that has not been synced yet
                         if (!unsyncedEncounterIds.contains(it.encounterForm.encounterId)) {
                             Completable.concat(listOf(
                                encounterFormRepository.sync(encounterFormDelta),
                                deltaRepository.markAsSynced(listOf(encounterFormDelta))
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
