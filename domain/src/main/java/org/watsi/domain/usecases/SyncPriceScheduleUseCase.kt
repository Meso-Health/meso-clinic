package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Delta
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.PriceScheduleRepository

class SyncPriceScheduleUseCase(
        private val priceScheduleRepository: PriceScheduleRepository,
        private val deltaRepository: DeltaRepository
) {
    fun execute(onError: (throwable: Throwable) -> Boolean): Completable {
        return Completable.fromAction {
            val unsyncedPriceScheduleDeltas = deltaRepository.unsynced(Delta.ModelName.PRICE_SCHEDULE).blockingGet()

            unsyncedPriceScheduleDeltas.forEach { priceScheduleDelta ->
                Completable.concatArray(
                    priceScheduleRepository.sync(priceScheduleDelta),
                    deltaRepository.markAsSynced(listOf(priceScheduleDelta))
                ).onErrorComplete {
                    onError(it)
                }.blockingAwait()
            }
        }.subscribeOn(Schedulers.io())
    }
}
