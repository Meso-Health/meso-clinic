package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DeltaRepository

class SyncBillableUseCase(
        private val billableRepository: BillableRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(): Completable {
        return Completable.fromAction {
            val unsyncedBillableDeltas = deltaRepository.unsynced(Delta.ModelName.BILLABLE).blockingGet()

            unsyncedBillableDeltas.map { billableDelta ->
                billableRepository.sync(billableDelta).blockingAwait()
                deltaRepository.markAsSynced(listOf(billableDelta)).blockingAwait()
            }
        }
    }
}
