package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.IdentificationEventRepository

class SyncIdentificationEventUseCase(
        private val identificationEventRepository: IdentificationEventRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedIdEventDeltas = deltaRepository.unsynced(
                    Delta.ModelName.IDENTIFICATION_EVENT).blockingGet()

            unsyncedIdEventDeltas.map { idEventDelta ->
                Completable.concatArray(
                    identificationEventRepository.sync(idEventDelta),
                    deltaRepository.markAsSynced(listOf(idEventDelta))
                ).onErrorComplete {
                    onError(it)
                }.blockingAwait()

            }
        }.subscribeOn(Schedulers.io())
    }
}
