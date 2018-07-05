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
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.MemberRepository

@RunWith(MockitoJUnitRunner::class)
class SyncMemberPhotoUseCaseTest {

    @Mock lateinit var memberRepo: MemberRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    lateinit var useCaseMember: SyncMemberPhotoUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        useCaseMember = SyncMemberPhotoUseCase(memberRepo, deltaRepo)
    }

    @Test
    fun execute() {
        val syncedMember = MemberFactory.build()
        val unsyncedMember = MemberFactory.build()
        val shouldBeSyncedPhotoDelta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PHOTO,
                modelId = syncedMember.id,
                synced = false
        )
        val shouldNotBeSyncedPhotoDelta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PHOTO,
                modelId = unsyncedMember.id,
                synced = false
        )
        whenever(deltaRepo.unsyncedModelIds(Delta.ModelName.MEMBER, Delta.Action.ADD))
                .thenReturn(Single.just(listOf(unsyncedMember.id)))
        whenever(deltaRepo.unsynced(Delta.ModelName.PHOTO))
                .thenReturn(Single.just(listOf(shouldBeSyncedPhotoDelta,
                                               shouldNotBeSyncedPhotoDelta)))
        whenever(memberRepo.syncPhotos(listOf(shouldBeSyncedPhotoDelta)))
                .thenReturn(Completable.complete())
        whenever(deltaRepo.markAsSynced(listOf(shouldBeSyncedPhotoDelta)))
                .thenReturn(Completable.complete())

        useCaseMember.execute().test().assertComplete()
    }
}
