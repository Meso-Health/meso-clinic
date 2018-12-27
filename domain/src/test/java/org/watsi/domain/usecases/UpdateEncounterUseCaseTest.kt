package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import io.reactivex.Single
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
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.DiagnosisFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableAndPriceFactory
import org.watsi.domain.factories.EncounterWithItemsAndFormsFactory
import org.watsi.domain.relations.EncounterWithItems
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.PriceScheduleRepository

@RunWith(MockitoJUnitRunner::class)
class UpdateEncounterUseCaseTest {

    @Mock lateinit var mockEncounterRepository: EncounterRepository
    @Mock lateinit var mockPriceScheduleRepository: PriceScheduleRepository
    lateinit var useCase: UpdateEncounterUseCase
    lateinit var fixedClock: Clock

    private val diagnosis1 = DiagnosisFactory.build(id = 1, description = "Malaria")
    private val diagnosis2 = DiagnosisFactory.build(id = 2, description = "Intestinal Worms")
    private val serviceBillable1 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Service A", type = Billable.Type.SERVICE))
    private val serviceBillable2 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Service B", type = Billable.Type.SERVICE))
    private val labBillable1 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Lab A", type = Billable.Type.LAB))
    private val labBillable2 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Lab B", type = Billable.Type.LAB))
    private val drugBillable1 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Drug A", type = Billable.Type.DRUG))
    private val drugBillable2 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Drug B", type = Billable.Type.DRUG))
    private val drugBillable3 = BillableWithPriceScheduleFactory.build(BillableFactory.build(name = "Drug C", type = Billable.Type.DRUG))
    private val savedEncounter = EncounterFactory.build(
        diagnoses = listOf(diagnosis1.id),
        providerComment = "comment"
    )
    private val service1EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        billableId = serviceBillable1.billable.id,
        priceScheduleId = serviceBillable1.priceSchedule.id,
        quantity = 1
    )
    private val service2EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        billableId = serviceBillable2.billable.id,
        priceScheduleId = serviceBillable2.priceSchedule.id,
        quantity = 1
    )
    private val lab1EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        billableId = labBillable1.billable.id,
        priceScheduleId = labBillable1.priceSchedule.id,
        quantity = 1
    )
    private val lab2EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        billableId = labBillable2.billable.id,
        priceScheduleId = labBillable2.priceSchedule.id,
        quantity = 1
    )
    private val drug1EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        billableId = drugBillable1.billable.id,
        priceScheduleId = drugBillable1.priceSchedule.id,
        quantity = 5
    )
    private val drug2EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        billableId = drugBillable2.billable.id,
        priceScheduleId = drugBillable2.priceSchedule.id,
        quantity = 10
    )
    private val drug3EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        billableId = drugBillable3.billable.id,
        priceScheduleId = drugBillable3.priceSchedule.id,
        quantity = 20
    )
    private val savedEncounterWithItems = EncounterWithItems(
        encounter = savedEncounter,
        encounterItems = listOf(
            service1EncounterItem,
            lab1EncounterItem,
            drug1EncounterItem,
            drug2EncounterItem
        )
    )
    private val updatedEncounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
        encounter = savedEncounter.copy(
            diagnoses = listOf(diagnosis2.id),
            providerComment = "changed comment"
        ),
        encounterItemRelations = listOf(
            EncounterItemWithBillableAndPriceFactory.build(
                billableWithPrice = serviceBillable2,
                encounterItem = service2EncounterItem
            ),
            EncounterItemWithBillableAndPriceFactory.build(
                billableWithPrice = labBillable1,
                encounterItem = lab1EncounterItem
            ),
            EncounterItemWithBillableAndPriceFactory.build(
                billableWithPrice = labBillable2,
                encounterItem = lab2EncounterItem
            ),
            EncounterItemWithBillableAndPriceFactory.build(
                billableWithPrice = drugBillable1,
                encounterItem = drug1EncounterItem.copy(quantity = 10)
            ),
            EncounterItemWithBillableAndPriceFactory.build(
                billableWithPrice = drugBillable3,
                encounterItem = drug3EncounterItem
            )
        )
    )
    private val removedEncounterItemIds = listOf(service1EncounterItem, drug2EncounterItem).map { it.id }

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = UpdateEncounterUseCase(mockEncounterRepository, mockPriceScheduleRepository)
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    }

    @Test
    fun execute_encounterDoesNotHaveNewPriceSchedules_updatesEncounterAndEncounterItems() {
        updatedEncounterWithItemsAndForms.encounterItemRelations.map {
            val priceSchedule = it.billableWithPriceSchedule.priceSchedule
            whenever(mockPriceScheduleRepository.find(priceSchedule.id)).thenReturn(Maybe.just(priceSchedule))
        }
        whenever(mockEncounterRepository.find(savedEncounter.id)).thenReturn(
            Single.just(savedEncounterWithItems)
        )
        whenever(mockEncounterRepository.upsert(updatedEncounterWithItemsAndForms)).thenReturn(
            Completable.complete()
        )
        whenever(mockEncounterRepository.deleteEncounterItems(removedEncounterItemIds)).thenReturn(
            Completable.complete()
        )

        useCase.execute(updatedEncounterWithItemsAndForms).test().assertComplete()
    }

    @Test
    fun execute_encounterHasNewPriceSchedules_updatesEncounterAndEncounterItemsAndCreatesPriceSchedules() {
        updatedEncounterWithItemsAndForms.encounterItemRelations.map {
            val priceSchedule = it.billableWithPriceSchedule.priceSchedule
            val priceScheduleDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PRICE_SCHEDULE,
                modelId = priceSchedule.id
            )

            whenever(mockPriceScheduleRepository.find(priceSchedule.id)).thenReturn(Maybe.empty())
            whenever(mockPriceScheduleRepository.create(priceSchedule, priceScheduleDelta)).thenReturn(Completable.complete())
        }
        whenever(mockEncounterRepository.find(savedEncounter.id)).thenReturn(
            Single.just(savedEncounterWithItems)
        )
        whenever(mockEncounterRepository.upsert(updatedEncounterWithItemsAndForms)).thenReturn(
            Completable.complete()
        )
        whenever(mockEncounterRepository.deleteEncounterItems(removedEncounterItemIds)).thenReturn(
            Completable.complete()
        )

        useCase.execute(updatedEncounterWithItemsAndForms).test().assertComplete()
    }
}
