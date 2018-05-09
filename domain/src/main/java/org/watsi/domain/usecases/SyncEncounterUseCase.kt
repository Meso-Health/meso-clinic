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
        return deltaRepository.unsynced(Delta.ModelName.ENCOUNTER).flatMapCompletable { encounterDeltas ->
            Completable.concat(encounterDeltas.groupBy { it.modelId }.values.map { groupedDeltas ->
                Completable.concat(listOf(
                        encounterRepository.sync(groupedDeltas),
                        deltaRepository.markAsSynced(groupedDeltas)
                ))
            })
        }
    }
}
