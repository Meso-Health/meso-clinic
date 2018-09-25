package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableAndPriceFactory
import org.watsi.domain.factories.EncounterWithItemsAndFormsFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.factories.PriceScheduleFactory
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class SubmitMemberAndClaimUseCaseTest {

    @Mock lateinit var mockDeltaRepository: DeltaRepository
    @Mock lateinit var mockEncounterRepository: EncounterRepository
    lateinit var useCase: SubmitMemberAndClaimUseCase
    lateinit var clock: Clock

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = SubmitMemberAndClaimUseCase(mockDeltaRepository, mockEncounterRepository)
        clock = Clock.systemDefaultZone()
    }

    @Test
    fun execute_submitEncounterDoesNotHaveNewBillablesOrEncounterForms_createsEncounterWithDelta() {
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build()
        val member = MemberFactory.build(photoId = null)
        val encounterDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = encounterWithItemsAndForms.encounter.id
        )
        val memberDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBER,
            modelId = member.id
        )

        whenever(mockEncounterRepository.update(listOf(encounterWithItemsAndForms.encounter)))
            .thenReturn(Completable.complete())
        whenever(mockDeltaRepository.insert(listOf(memberDelta, encounterDelta)))
            .thenReturn(Completable.complete())

        useCase.execute(member, encounterWithItemsAndForms, clock).test().assertComplete()
    }

    @Test
    fun execute_encounterDoesNotHaveNewBillablesOrEncounterFormsHasMemberPhoto_createsEncounterWithDeltaNoPhotoDelta() {
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build()
        val member = MemberFactory.build(photoId = UUID.randomUUID())
        val encounterDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = encounterWithItemsAndForms.encounter.id
        )
        val memberDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBER,
            modelId = member.id
        )

        whenever(mockEncounterRepository.update(listOf(encounterWithItemsAndForms.encounter)))
            .thenReturn(Completable.complete())
        whenever(mockDeltaRepository.insert(listOf(memberDelta, encounterDelta)))
            .thenReturn(Completable.complete())

        useCase.execute(member, encounterWithItemsAndForms, clock).test().assertComplete()
    }

    @Test
    fun execute_encounterHasNewPriceSchedules_createsEncounterWithDeltaAndPriceSchedulesWithDeltas() {
        val encounter = EncounterFactory.build()
        val member = MemberFactory.build()
        val billable = BillableFactory.build()
        val priceSchedule = PriceScheduleFactory.build(billableId = billable.id)
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id, billableId = billable.id, priceScheduleIssued = true)
        val encounterItemRelation = EncounterItemWithBillableAndPriceFactory.build(
            BillableWithPriceScheduleFactory.build(billable, priceSchedule), encounterItem
        )
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
            encounter = encounter,
            encounterItemRelations = listOf(encounterItemRelation)
        )
        val encounterDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = encounter.id
        )
        val priceScheduleDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.PRICE_SCHEDULE,
            modelId = priceSchedule.id
        )
        val memberDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBER,
            modelId = member.id
        )

        whenever(mockEncounterRepository.update(listOf(encounterWithItemsAndForms.encounter)))
            .thenReturn(Completable.complete())
        whenever(mockDeltaRepository.insert(listOf(memberDelta, encounterDelta, priceScheduleDelta)))
            .thenReturn(Completable.complete())

        useCase.execute(member, encounterWithItemsAndForms, clock).test().assertComplete()
    }

    @Test
    fun execute_encounterHasEncounterForms_createsEncounterWithDeltaAndEncounterFormDeltas() {
        val encounter = EncounterFactory.build()
        val member = MemberFactory.build()
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
            encounter = encounter,
            forms = listOf(encounterForm)
        )
        val encounterDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = encounter.id
        )
        val encounterFormDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER_FORM,
            modelId = encounterForm.id
        )
        val memberDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.MEMBER,
            modelId = member.id
        )

        whenever(mockEncounterRepository.update(listOf(encounterWithItemsAndForms.encounter)))
            .thenReturn(Completable.complete())
        whenever(mockDeltaRepository.insert(listOf(memberDelta, encounterDelta, encounterFormDelta)))
            .thenReturn(Completable.complete())

        useCase.execute(member, encounterWithItemsAndForms, clock).test().assertComplete()
    }
}