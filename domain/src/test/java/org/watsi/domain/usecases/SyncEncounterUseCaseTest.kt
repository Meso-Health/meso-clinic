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
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterWithItemsFactory
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository

@RunWith(MockitoJUnitRunner::class)
class SyncEncounterUseCaseTest {

    @Mock lateinit var encounterRepo: EncounterRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    lateinit var usecase: SyncEncounterUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        usecase = SyncEncounterUseCase(encounterRepo, deltaRepo)
    }

    @Test
    fun execute() {
        val syncedIdEvent = IdentificationEventFactory.build()
        val shouldBeSyncedEncounter = EncounterFactory.build(identificationEventId = syncedIdEvent.id)
        val shouldBeSyncedEncounterDelta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = shouldBeSyncedEncounter.id,
                synced = false
        )

        val unsyncedIdEvent = IdentificationEventFactory.build()
        val shouldNotBeSyncedEncounter = EncounterFactory.build(identificationEventId = unsyncedIdEvent.id)
        val shouldNotBeSyncedEncounterDelta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = shouldNotBeSyncedEncounter.id,
                synced = false
        )

        whenever(deltaRepo.unsyncedModelIds(Delta.ModelName.IDENTIFICATION_EVENT, Delta.Action.ADD))
                .thenReturn(Single.just(listOf(unsyncedIdEvent.id)))
        whenever(deltaRepo.unsynced(Delta.ModelName.ENCOUNTER))
                .thenReturn(Single.just(listOf(shouldBeSyncedEncounterDelta, shouldNotBeSyncedEncounterDelta)))
        whenever(encounterRepo.find(shouldBeSyncedEncounter.id))
                .thenReturn(Single.just(EncounterWithItemsFactory.build(encounter = shouldBeSyncedEncounter)))
        whenever(encounterRepo.find(shouldNotBeSyncedEncounter.id))
                .thenReturn(Single.just(EncounterWithItemsFactory.build(encounter = shouldNotBeSyncedEncounter)))
        whenever(encounterRepo.sync(shouldBeSyncedEncounterDelta))
                .thenReturn(Completable.complete())
        whenever(deltaRepo.markAsSynced(listOf(shouldBeSyncedEncounterDelta)))
                .thenReturn(Completable.complete())

        usecase.execute().test().assertComplete()
    }
}
