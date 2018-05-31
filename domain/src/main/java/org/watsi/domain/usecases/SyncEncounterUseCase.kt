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
        return Completable.fromAction {
            val unsyncedEncounterDeltas = deltaRepository.unsynced(
                    Delta.ModelName.ENCOUNTER).blockingGet()
            val unsyncedIdEventIds = deltaRepository.unsyncedModelIds(
                    Delta.ModelName.IDENTIFICATION_EVENT, Delta.Action.ADD).blockingGet()
            val unsyncedBillableIds = deltaRepository.unsyncedModelIds(
                    Delta.ModelName.BILLABLE, Delta.Action.ADD).blockingGet()

            unsyncedEncounterDeltas.map { encounterDelta ->
                val encounterWithItems = encounterRepository.find(encounterDelta.modelId).blockingGet()
                val hasUnsyncedIdEvent = unsyncedIdEventIds.contains(encounterWithItems.encounter.identificationEventId)
                val hasUnsyncedBillable = encounterWithItems.encounterItems.any { unsyncedBillableIds.contains(it.billableId) }

                if (!hasUnsyncedIdEvent && !hasUnsyncedBillable) {
                    encounterRepository.sync(encounterDelta).blockingGet()
                    deltaRepository.markAsSynced(listOf(encounterDelta)).blockingGet()
                }
            }
        }
    }
}
