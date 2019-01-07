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
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.MemberRepository

@RunWith(MockitoJUnitRunner::class)
class SyncMemberPhotoUseCaseTest {

    @Mock lateinit var memberRepo: MemberRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    @Mock lateinit var exception: Exception
    @Mock lateinit var onErrorCallback: (throwable: Throwable) -> Boolean

    lateinit var useCaseMember: SyncMemberPhotoUseCase
    private val syncedMember = MemberFactory.build()
    private val unsyncedMember = MemberFactory.build()
    private val shouldBeSyncedPhotoDelta = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.PHOTO,
        modelId = syncedMember.id,
        synced = false
    )
    private val shouldNotBeSyncedPhotoDelta = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.PHOTO,
        modelId = unsyncedMember.id,
        synced = false
    )
    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        whenever(onErrorCallback.invoke(any())).thenReturn(true)
        whenever(deltaRepo.unsyncedModelIds(Delta.ModelName.MEMBER, Delta.Action.ADD))
                .thenReturn(Single.just(listOf(unsyncedMember.id)))
        whenever(deltaRepo.unsynced(Delta.ModelName.PHOTO))
                .thenReturn(Single.just(listOf(shouldBeSyncedPhotoDelta,
                    shouldNotBeSyncedPhotoDelta)))
        whenever(deltaRepo.markAsSynced(listOf(shouldBeSyncedPhotoDelta)))
                .thenReturn(Completable.complete())
        useCaseMember = SyncMemberPhotoUseCase(memberRepo, deltaRepo)
    }

    @Test
    fun execute_success() {
        whenever(memberRepo.syncPhotos(listOf(shouldBeSyncedPhotoDelta)))
                .thenReturn(Completable.complete())
        useCaseMember.execute(onErrorCallback).test().assertComplete()
    }

    @Test
    fun execute_failure() {
        whenever(memberRepo.syncPhotos(listOf(shouldBeSyncedPhotoDelta)))
                .thenReturn(Completable.error(exception))
        useCaseMember.execute(onErrorCallback).test().assertComplete()
        verify(onErrorCallback, times(1)).invoke(exception)
    }
}
