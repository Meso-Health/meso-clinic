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
        return Completable.fromAction {
            val unsyncedEncounterFormDeltas = deltaRepository.unsynced(
                    Delta.ModelName.ENCOUNTER_FORM).blockingGet()
            val unsyncedEncounterIds = deltaRepository.unsyncedModelIds(
                    Delta.ModelName.ENCOUNTER, Delta.Action.ADD).blockingGet()

            unsyncedEncounterFormDeltas.map { encounterFormDelta ->
                val encounterForm = encounterFormRepository.find(encounterFormDelta.modelId).blockingGet()
                val hasUnsyncedEncounter = unsyncedEncounterIds.contains(encounterForm.encounterForm.encounterId)

                if (!hasUnsyncedEncounter) {
                    encounterFormRepository.sync(encounterFormDelta).blockingAwait()
                    deltaRepository.markAsSynced(listOf(encounterFormDelta)).blockingAwait()
                }
            }
        }
    }
}
