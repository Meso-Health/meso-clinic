package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DeltaRepository

class SyncBillableUseCase(
        private val billableRepository: BillableRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedBillableDeltas = deltaRepository.unsynced(Delta.ModelName.BILLABLE).blockingGet()

            unsyncedBillableDeltas.map { billableDelta ->
                Completable.concatArray(
                    billableRepository.sync(billableDelta),
                    deltaRepository.markAsSynced(listOf(billableDelta))
                ).onErrorComplete {
                    onError(it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}
