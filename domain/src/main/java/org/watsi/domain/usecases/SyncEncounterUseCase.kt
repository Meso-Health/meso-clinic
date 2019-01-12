package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository

class SyncEncounterUseCase(
        private val encounterRepository: EncounterRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedEncounterDeltas = deltaRepository.unsynced(
                Delta.ModelName.ENCOUNTER).blockingGet()

            unsyncedEncounterDeltas.map { encounterDelta ->
                Completable.concatArray(
                    encounterRepository.sync(encounterDelta),
                    deltaRepository.markAsSynced(listOf(encounterDelta))
                ).onErrorComplete {
                    onError(it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}
