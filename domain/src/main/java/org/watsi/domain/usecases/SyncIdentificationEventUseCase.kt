package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.IdentificationEventRepository

class SyncIdentificationEventUseCase(
        private val identificationEventRepository: IdentificationEventRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(): Completable {
        return Completable.fromAction {
            val unsyncedIdEventDeltas = deltaRepository.unsynced(
                    Delta.ModelName.IDENTIFICATION_EVENT).blockingGet()

            unsyncedIdEventDeltas.map { idEventDelta ->
                identificationEventRepository.sync(idEventDelta).blockingAwait()
                deltaRepository.markAsSynced(listOf(idEventDelta)).blockingAwait()
            }
        }
    }
}
