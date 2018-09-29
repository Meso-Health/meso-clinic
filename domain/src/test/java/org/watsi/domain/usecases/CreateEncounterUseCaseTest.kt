package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableAndPriceFactory
import org.watsi.domain.factories.EncounterWithItemsAndFormsFactory
import org.watsi.domain.factories.PriceScheduleFactory
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.PriceScheduleRepository

@RunWith(MockitoJUnitRunner::class)
class CreateEncounterUseCaseTest {

    @Mock lateinit var mockEncounterRepository: EncounterRepository
    @Mock lateinit var mockBillableRepository: BillableRepository
    @Mock lateinit var mockPriceScheduleRepository: PriceScheduleRepository
    lateinit var useCase: CreateEncounterUseCase
    lateinit var fixedClock: Clock

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = CreateEncounterUseCase(mockEncounterRepository, mockBillableRepository, mockPriceScheduleRepository)
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    }

    @Test
    fun execute_encounterDoesNotHaveNewBillablesOrEncounterForms_submitNowTrue_createsEncounterWithDeltaAndSetsSubmittedAt() {
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build()
        val encounterDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = encounterWithItemsAndForms.encounter.id
        )

        val encounterWithItemsAndFormsAndTimestamps = encounterWithItemsAndForms.copy(
            encounter = encounterWithItemsAndForms.encounter.copy(
                preparedAt = Instant.now(fixedClock),
                submittedAt = Instant.now(fixedClock)
            )
        )

        whenever(mockEncounterRepository.insert(encounterWithItemsAndFormsAndTimestamps, listOf(encounterDelta)))
                .thenReturn(Completable.complete())

        useCase.execute(encounterWithItemsAndForms, true, fixedClock).test().assertComplete()
    }

    @Test
    fun execute_encounterDoesNotHaveNewBillablesOrEncounterForms_submitNowFalse_createsEncounterWithoutDeltaAndWithoutSubmittedAt() {
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build()

        val encounterWithItemsAndFormsAndTimestamps = encounterWithItemsAndForms.copy(
            encounter = encounterWithItemsAndForms.encounter.copy(
                preparedAt = Instant.now(fixedClock)
            )
        )

        whenever(mockEncounterRepository.insert(encounterWithItemsAndFormsAndTimestamps, emptyList()))
            .thenReturn(Completable.complete())

        useCase.execute(encounterWithItemsAndForms, false, fixedClock).test().assertComplete()
    }

    @Test
    fun execute_encounterHasNewBillables_submitNowTrue_createsEncounterWithDeltaAndBillablesWithDeltasAndSetsSubmittedAt() {
        val encounter = EncounterFactory.build()
        val billable = BillableFactory.build()
        val priceSchedule = PriceScheduleFactory.build(billableId = billable.id)
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id, billableId = billable.id)
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
        val billableDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.BILLABLE,
            modelId = billable.id
        )
        val priceScheduleDelta = Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.PRICE_SCHEDULE,
            modelId = priceSchedule.id
        )

        val encounterWithItemsAndFormsAndTimestamps = encounterWithItemsAndForms.copy(
            encounter = encounterWithItemsAndForms.encounter.copy(
                preparedAt = Instant.now(fixedClock),
                submittedAt = Instant.now(fixedClock)
            )
        )

        whenever(mockBillableRepository.find(billable.id)).thenReturn(Maybe.empty())
        whenever(mockBillableRepository.create(billable, billableDelta))
            .thenReturn(Completable.complete())
        whenever(mockPriceScheduleRepository.create(priceSchedule, priceScheduleDelta))
            .thenReturn(Completable.complete())
        whenever(mockEncounterRepository.insert(encounterWithItemsAndFormsAndTimestamps, listOf(encounterDelta)))
            .thenReturn(Completable.complete())

        useCase.execute(encounterWithItemsAndForms, true, fixedClock).test().assertComplete()
    }

    @Test
    fun execute_encounterHasNewPriceSchedules_submitNowTrue_createsEncounterWithDeltaAndPriceSchedulesWithDeltasAndSetsSubmittedAt() {
        val encounter = EncounterFactory.build()
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

        val encounterWithItemsAndFormsAndTimestamps = encounterWithItemsAndForms.copy(
            encounter = encounterWithItemsAndForms.encounter.copy(
                preparedAt = Instant.now(fixedClock),
                submittedAt = Instant.now(fixedClock)
            )
        )

        whenever(mockBillableRepository.find(billable.id)).thenReturn(Maybe.just(billable))
        whenever(mockPriceScheduleRepository.find(priceSchedule.id)).thenReturn(Maybe.empty())
        whenever(mockPriceScheduleRepository.create(priceSchedule, priceScheduleDelta))
            .thenReturn(Completable.complete())
        whenever(mockEncounterRepository.insert(encounterWithItemsAndFormsAndTimestamps, listOf(encounterDelta)))
            .thenReturn(Completable.complete())

        useCase.execute(encounterWithItemsAndForms, true, fixedClock).test().assertComplete()
    }

    @Test
    fun execute_encounterHasEncounterForms_submitNowTrue_createsEncounterWithDeltaAndEncounterFormDeltasAndSetsSubmittedAt() {
        val encounter = EncounterFactory.build()
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

        val encounterWithItemsAndFormsAndTimestamps = encounterWithItemsAndForms.copy(
            encounter = encounterWithItemsAndForms.encounter.copy(
                preparedAt = Instant.now(fixedClock),
                submittedAt = Instant.now(fixedClock)
            )
        )

        whenever(mockEncounterRepository.insert(encounterWithItemsAndFormsAndTimestamps, listOf(encounterDelta, encounterFormDelta)))
                .thenReturn(Completable.complete())

        useCase.execute(encounterWithItemsAndForms, true, fixedClock).test().assertComplete()
    }
}
