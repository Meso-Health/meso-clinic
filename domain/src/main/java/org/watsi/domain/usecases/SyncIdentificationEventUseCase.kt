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
            val unsyncedIdEventDeltas = deltaRepository.unsynced(Delta.ModelName.IDENTIFICATION_EVENT).blockingGet()
            unsyncedIdEventDeltas.groupBy { it.modelId }.values.map { idEventDeltas ->
                Completable.fromAction {
                    identificationEventRepository.sync(idEventDeltas).blockingAwait()
                    deltaRepository.markAsSynced(idEventDeltas).blockingAwait()
                }.onErrorComplete {
                    // throw inner exception if it exists because blockingAwait typically
                    // wraps the exception in a RuntimeException
                    onError(it.cause ?: it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}
