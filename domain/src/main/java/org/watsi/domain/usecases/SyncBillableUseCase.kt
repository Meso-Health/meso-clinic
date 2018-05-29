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
        return deltaRepository.unsynced(Delta.ModelName.BILLABLE).flatMapCompletable { billableDeltas ->
            Completable.concat(billableDeltas.map { billableDelta ->
                Completable.concat(listOf(
                        billableRepository.sync(billableDelta),
                        deltaRepository.markAsSynced(listOf(billableDelta))
                ))
            })
        }
    }
}
