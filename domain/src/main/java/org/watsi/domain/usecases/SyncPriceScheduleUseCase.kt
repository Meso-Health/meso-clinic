package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.PriceScheduleRepository

class SyncPriceScheduleUseCase(
        private val priceScheduleRepository: PriceScheduleRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(): Completable {
        return Completable.fromAction {
            val unsyncedPriceScheduleDeltas = deltaRepository.unsynced(Delta.ModelName.PRICE_SCHEDULE).blockingGet()

            unsyncedPriceScheduleDeltas.map { priceScheduleDelta ->
                priceScheduleRepository.sync(priceScheduleDelta).blockingAwait()
                deltaRepository.markAsSynced(listOf(priceScheduleDelta)).blockingAwait()
            }
        }
    }
}
