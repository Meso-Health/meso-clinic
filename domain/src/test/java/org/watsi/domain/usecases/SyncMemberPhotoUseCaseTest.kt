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
import org.watsi.domain.repositories.MemberRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class SyncMemberPhotoUseCaseTest {

    @Mock lateinit var memberRepo: MemberRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    @Mock lateinit var exception: Exception
    @Mock lateinit var onErrorCallback: (throwable: Throwable) -> Boolean

    lateinit var useCase: SyncMemberPhotoUseCase
    private val delta1 = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.PHOTO,
        modelId = UUID.randomUUID(),
        synced = false
    )
    private val delta2 = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.PHOTO,
        modelId = delta1.modelId,
        synced = false
    )
    private val delta3 = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.PHOTO,
        modelId = UUID.randomUUID(),
        synced = false
    )

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        whenever(onErrorCallback.invoke(any())).thenReturn(true)
        whenever(deltaRepo.unsynced(Delta.ModelName.PHOTO))
                .thenReturn(Single.just(listOf(delta1, delta2, delta3)))
        whenever(deltaRepo.markAsSynced(any())).thenReturn(Completable.complete())
        useCase = SyncMemberPhotoUseCase(memberRepo, deltaRepo)
    }

    @Test
    fun execute_success() {
        whenever(memberRepo.syncPhotos(any())).thenReturn(Completable.complete())
        useCase.execute(onErrorCallback).test().assertComplete()
        verify(memberRepo).syncPhotos(listOf(delta1, delta2))
        verify(memberRepo).syncPhotos(listOf(delta3))
    }

    @Test
    fun execute_failure() {
        whenever(memberRepo.syncPhotos(any())).thenReturn(Completable.error(exception))
        useCase.execute(onErrorCallback).test().assertComplete()
        verify(onErrorCallback, times(2)).invoke(exception)
    }
}
