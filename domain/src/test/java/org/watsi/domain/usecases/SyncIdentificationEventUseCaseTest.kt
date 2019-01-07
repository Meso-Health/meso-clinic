package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
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
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.IdentificationEventRepository

@RunWith(MockitoJUnitRunner::class)
class SyncIdentificationEventUseCaseTest {

    @Mock lateinit var identificationEventRepo: IdentificationEventRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    @Mock lateinit var onErrorCallback: (throwable: Throwable) -> Boolean
    @Mock lateinit var exception: Exception

    lateinit var useCase: SyncIdentificationEventUseCase

    val delta = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.IDENTIFICATION_EVENT,
        synced = false
    )
    val deltaList = listOf(delta)
    val unsyncedSingle = Single.just(deltaList)

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        whenever(onErrorCallback.invoke(any())).thenReturn(true)
        whenever(deltaRepo.unsynced(Delta.ModelName.IDENTIFICATION_EVENT)).thenReturn(unsyncedSingle)
        whenever(deltaRepo.markAsSynced(deltaList)).thenReturn(Completable.complete())

        useCase = SyncIdentificationEventUseCase(identificationEventRepo, deltaRepo)
    }

    @Test
    fun execute_success() {
        whenever(identificationEventRepo.sync(delta)).thenReturn(Completable.complete())
        useCase.execute(onErrorCallback).test().assertComplete()
    }

    @Test
    fun execute_failure() {
        whenever(identificationEventRepo.sync(delta)).thenReturn(Completable.error(exception))
        useCase.execute(onErrorCallback).test().assertComplete()

        verify(onErrorCallback, times(1)).invoke(exception)
    }
}
