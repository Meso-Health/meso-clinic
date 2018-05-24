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
        return deltaRepository.unsynced(Delta.ModelName.IDENTIFICATION_EVENT).flatMapCompletable { idEventDeltas ->
            Completable.concat(idEventDeltas.map { idEventDelta ->
                Completable.concat(listOf(
                        identificationEventRepository.sync(idEventDelta),
                        deltaRepository.markAsSynced(listOf(idEventDelta))
                ))
            })
        }
    }
}
