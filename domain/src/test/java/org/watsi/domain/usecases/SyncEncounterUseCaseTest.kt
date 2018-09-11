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
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterWithItemsFactory
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.factories.PriceScheduleFactory
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
        val unsyncedIdEvent = IdentificationEventFactory.build()
        val syncedBillable = BillableFactory.build()
        val unsyncedBillable = BillableFactory.build()
        val syncedPriceSchedule = PriceScheduleFactory.build()
        val unsyncedPriceSchedule = PriceScheduleFactory.build()
        val syncedMember = MemberFactory.build()
        val unsyncedMember = MemberFactory.build()
        val unsyncedEncounter1 = EncounterWithItemsFactory.buildWithBillableAndPriceSchedule(
            EncounterFactory.build(
                memberId = syncedMember.id,
                identificationEventId = syncedIdEvent.id
            ),
            syncedBillable,
            syncedPriceSchedule
        )
        val unsyncedEncounter2 = EncounterWithItemsFactory.buildWithBillableAndPriceSchedule(
            EncounterFactory.build(
                memberId = syncedMember.id,
                identificationEventId = unsyncedIdEvent.id
            ),
            syncedBillable,
            syncedPriceSchedule
        )
        val unsyncedEncounter3 = EncounterWithItemsFactory.buildWithBillableAndPriceSchedule(
            EncounterFactory.build(
                memberId = unsyncedMember.id,
                identificationEventId = syncedIdEvent.id
            ),
            syncedBillable,
            syncedPriceSchedule
        )
        val unsyncedEncounter4 = EncounterWithItemsFactory.buildWithBillableAndPriceSchedule(
            EncounterFactory.build(
                memberId = syncedMember.id,
                identificationEventId = syncedIdEvent.id
            ),
            unsyncedBillable,
            syncedPriceSchedule
        )
        val unsyncedEncounter5 = EncounterWithItemsFactory.buildWithBillableAndPriceSchedule(
            EncounterFactory.build(
                memberId = syncedMember.id,
                identificationEventId = syncedIdEvent.id
            ),
            syncedBillable,
            unsyncedPriceSchedule
        )
        val unsyncedEncounter6 = EncounterWithItemsFactory.buildWithBillableAndPriceSchedule(
            EncounterFactory.build(
                memberId = syncedMember.id,
                identificationEventId = null
            ),
            syncedBillable,
            syncedPriceSchedule
        )
        val unsyncedEncounterDelta1 = DeltaFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = unsyncedEncounter1.encounter.id,
            synced = false
        )
        val unsyncedEncounterDelta2 = DeltaFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = unsyncedEncounter2.encounter.id,
            synced = false
        )
        val unsyncedEncounterDelta3 = DeltaFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = unsyncedEncounter3.encounter.id,
            synced = false
        )
        val unsyncedEncounterDelta4 = DeltaFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = unsyncedEncounter4.encounter.id,
            synced = false
        )
        val unsyncedEncounterDelta5 = DeltaFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = unsyncedEncounter5.encounter.id,
            synced = false
        )
        val unsyncedEncounterDelta6 = DeltaFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = unsyncedEncounter6.encounter.id,
            synced = false
        )
        // encounter with everything synced and encounter with null identification event should sync
        val shouldBeSyncedEncounterDelta1 = unsyncedEncounterDelta1
        val shouldBeSyncedEncounterDelta2 = unsyncedEncounterDelta6

        whenever(deltaRepo.unsynced(Delta.ModelName.ENCOUNTER))
                .thenReturn(Single.just(listOf(
                    unsyncedEncounterDelta1,
                    unsyncedEncounterDelta2,
                    unsyncedEncounterDelta3,
                    unsyncedEncounterDelta4,
                    unsyncedEncounterDelta5,
                    unsyncedEncounterDelta6
                )))
        whenever(deltaRepo.unsyncedModelIds(Delta.ModelName.IDENTIFICATION_EVENT, Delta.Action.ADD))
                .thenReturn(Single.just(listOf(unsyncedIdEvent.id)))
        whenever(deltaRepo.unsyncedModelIds(Delta.ModelName.BILLABLE, Delta.Action.ADD))
                .thenReturn(Single.just(listOf(unsyncedBillable.id)))
        whenever(deltaRepo.unsyncedModelIds(Delta.ModelName.PRICE_SCHEDULE, Delta.Action.ADD))
                .thenReturn(Single.just(listOf(unsyncedPriceSchedule.id)))
        whenever(deltaRepo.unsyncedModelIds(Delta.ModelName.MEMBER, Delta.Action.ADD))
                .thenReturn(Single.just(listOf(unsyncedMember.id)))

        whenever(encounterRepo.find(unsyncedEncounter1.encounter.id))
                .thenReturn(Single.just(unsyncedEncounter1))
        whenever(encounterRepo.find(unsyncedEncounter2.encounter.id))
                .thenReturn(Single.just(unsyncedEncounter2))
        whenever(encounterRepo.find(unsyncedEncounter3.encounter.id))
                .thenReturn(Single.just(unsyncedEncounter2))
        whenever(encounterRepo.find(unsyncedEncounter4.encounter.id))
                .thenReturn(Single.just(unsyncedEncounter4))
        whenever(encounterRepo.find(unsyncedEncounter5.encounter.id))
                .thenReturn(Single.just(unsyncedEncounter5))
        whenever(encounterRepo.find(unsyncedEncounter6.encounter.id))
                .thenReturn(Single.just(unsyncedEncounter6))

        whenever(encounterRepo.sync(shouldBeSyncedEncounterDelta1))
                .thenReturn(Completable.complete())
        whenever(encounterRepo.sync(shouldBeSyncedEncounterDelta2))
                .thenReturn(Completable.complete())
        whenever(deltaRepo.markAsSynced(listOf(shouldBeSyncedEncounterDelta1)))
                .thenReturn(Completable.complete())
        whenever(deltaRepo.markAsSynced(listOf(shouldBeSyncedEncounterDelta2)))
                .thenReturn(Completable.complete())

        usecase.execute().test().assertComplete()
    }
}
