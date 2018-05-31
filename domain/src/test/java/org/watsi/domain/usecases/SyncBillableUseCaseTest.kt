package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
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
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DeltaRepository

@RunWith(MockitoJUnitRunner::class)
class SyncBillableUseCaseTest {

    @Mock lateinit var billableRepo: BillableRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    lateinit var useCase: SyncBillableUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        useCase = SyncBillableUseCase(billableRepo, deltaRepo)
    }

    @Test
    fun execute() {
        val delta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.BILLABLE,
                synced = false
        )
        val deltaList = listOf(delta)
        val unsyncedSingle = Single.just(deltaList)
        whenever(deltaRepo.unsynced(Delta.ModelName.BILLABLE)).thenReturn(unsyncedSingle)
        whenever(billableRepo.sync(delta)).thenReturn(Completable.complete())
        whenever(deltaRepo.markAsSynced(deltaList)).thenReturn(Completable.complete())

        useCase.execute().test().assertComplete()
    }
}
