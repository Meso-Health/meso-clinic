package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
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
import org.watsi.domain.repositories.MemberRepository

@RunWith(MockitoJUnitRunner::class)
class SyncMemberUseCaseTest {

    @Mock lateinit var memberRepo: MemberRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    @Mock lateinit var exception: Exception
    @Mock lateinit var onErrorCallback: (throwable: Throwable) -> Boolean
    lateinit var useCase: SyncMemberUseCase

    private val delta = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.MEMBER,
        synced = false
    )
    private val deltaList = listOf(delta)
    private val unsyncedSingle = Single.just(deltaList)

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        whenever(onErrorCallback.invoke(any())).thenReturn(true)
        whenever(deltaRepo.unsynced(Delta.ModelName.MEMBER)).thenReturn(unsyncedSingle)
        whenever(deltaRepo.markAsSynced(deltaList)).thenReturn(Completable.complete())
        useCase = SyncMemberUseCase(memberRepo, deltaRepo)
    }

    @Test
    fun execute_success() {
        whenever(memberRepo.sync(deltaList)).thenReturn(Completable.complete())
        useCase.execute(onErrorCallback).test().assertComplete()
        verify(memberRepo, times(1)).sync(any())
        verify(deltaRepo, times(1)).markAsSynced(any())
    }

    @Test
    fun execute_syncFails_doesNotMarkAsSynced() {
        whenever(memberRepo.sync(deltaList)).thenReturn(Completable.error(exception))
        useCase.execute(onErrorCallback).test().assertComplete()
        verify(onErrorCallback, times(1)).invoke(exception)
        verify(memberRepo, times(1)).sync(any())
        verify(deltaRepo, never()).markAsSynced(any())
    }
}
