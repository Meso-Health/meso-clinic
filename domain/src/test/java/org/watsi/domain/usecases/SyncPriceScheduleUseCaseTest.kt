package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Scheduler
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.repositories.DeltaRepository

@RunWith(MockitoJUnitRunner::class)
class SyncPriceScheduleUseCaseTest {

    @Mock lateinit var priceScheduleRepository: PriceScheduleRepository
    @Mock lateinit var deltaRepository: DeltaRepository
    lateinit var useCase: SyncPriceScheduleUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        useCase = SyncPriceScheduleUseCase(priceScheduleRepository, deltaRepository)
    }

    @Test
    fun execute() {
        val delta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PRICE_SCHEDULE,
                synced = false
        )
        val deltaList = listOf(delta)
        val unsyncedSingle = Single.just(deltaList)
        whenever(deltaRepository.unsynced(Delta.ModelName.PRICE_SCHEDULE)).thenReturn(unsyncedSingle)
        whenever(priceScheduleRepository.sync(delta)).thenReturn(Completable.complete())
        whenever(deltaRepository.markAsSynced(deltaList)).thenReturn(Completable.complete())

        useCase.execute().test().assertComplete()
    }
}
