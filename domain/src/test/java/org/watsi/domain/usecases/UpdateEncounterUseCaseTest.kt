package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.any
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
import org.watsi.domain.factories.EncounterWithExtrasFactory
import org.watsi.domain.factories.MemberFactory
import org.watsi.domain.factories.ReferralFactory
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository
import org.watsi.domain.repositories.PriceScheduleRepository
import org.watsi.domain.repositories.ReferralRepository

@RunWith(MockitoJUnitRunner::class)
class UpdateEncounterUseCaseTest {

    @Mock lateinit var mockEncounterRepository: EncounterRepository
    @Mock lateinit var mockPriceScheduleRepository: PriceScheduleRepository
    @Mock lateinit var mockReferralRepository: ReferralRepository
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
        priceScheduleId = serviceBillable1.priceSchedule.id,
        quantity = 1
    )
    private val service2EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        priceScheduleId = serviceBillable2.priceSchedule.id,
        quantity = 1,
        priceScheduleIssued = true
    )
    private val lab1EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        priceScheduleId = labBillable1.priceSchedule.id,
        quantity = 1
    )
    private val lab2EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        priceScheduleId = labBillable2.priceSchedule.id,
        quantity = 1
    )
    private val drug1EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        priceScheduleId = drugBillable1.priceSchedule.id,
        quantity = 5,
        priceScheduleIssued = true
    )
    private val drug2EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        priceScheduleId = drugBillable2.priceSchedule.id,
        quantity = 10
    )
    private val drug3EncounterItem = EncounterItemFactory.build(
        encounterId = savedEncounter.id,
        priceScheduleId = drugBillable3.priceSchedule.id,
        quantity = 20
    )

    private val referral = ReferralFactory.build(
        encounterId = savedEncounter.id
    )

    private val savedEncounterWithExtras = EncounterWithExtras(
        encounter = savedEncounter,
        member = MemberFactory.build(id = savedEncounter.memberId),
        encounterItemRelations = listOf(
            EncounterItemWithBillableAndPrice(
                service1EncounterItem,
                serviceBillable1,
                null
            ),
            EncounterItemWithBillableAndPrice(
                lab1EncounterItem,
                labBillable1,
                null
            ),
            EncounterItemWithBillableAndPrice(
                drug1EncounterItem,
                drugBillable1,
                null
            ),
            EncounterItemWithBillableAndPrice(
                drug2EncounterItem,
                drugBillable2,
                null
            )
        ),
        encounterForms = emptyList(),
        referral = referral,
        diagnoses = emptyList()
    )

    private val updatedEncounterWithExtras = EncounterWithExtrasFactory.build(
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
    private val billablesWithIssuedPriceSchedules = listOf(serviceBillable2, drugBillable1)
    private val removedEncounterItemIds = listOf(service1EncounterItem, drug2EncounterItem).map { it.id }

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = UpdateEncounterUseCase(mockEncounterRepository, mockReferralRepository, mockPriceScheduleRepository)
        fixedClock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
        whenever(mockReferralRepository.delete(any())).thenReturn(Completable.complete())
    }

    @Test
    fun execute_encounterDoesNotHaveNewUnsavedPriceSchedules_updatesEncounterAndEncounterItems() {
        billablesWithIssuedPriceSchedules.map {
            val priceSchedule = it.priceSchedule
            whenever(mockPriceScheduleRepository.find(priceSchedule.id)).thenReturn(Maybe.just(priceSchedule))
        }

        whenever(mockEncounterRepository.find(savedEncounter.id)).thenReturn(
            Single.just(savedEncounterWithExtras)
        )
        whenever(mockEncounterRepository.upsert(updatedEncounterWithExtras)).thenReturn(
            Completable.complete()
        )
        whenever(mockEncounterRepository.deleteEncounterItems(removedEncounterItemIds)).thenReturn(
            Completable.complete()
        )

        useCase.execute(updatedEncounterWithExtras).test().assertComplete()
    }

    @Test
    fun execute_encounterHasNewUnsavedPriceSchedules_updatesEncounterAndEncounterItemsAndCreatesPriceSchedules() {
        billablesWithIssuedPriceSchedules.map {
            val priceSchedule = it.priceSchedule
            val priceScheduleDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.PRICE_SCHEDULE,
                modelId = priceSchedule.id
            )

            whenever(mockPriceScheduleRepository.find(priceSchedule.id)).thenReturn(Maybe.empty())
            whenever(mockPriceScheduleRepository.create(priceSchedule, priceScheduleDelta)).thenReturn(Completable.complete())
        }
        whenever(mockEncounterRepository.find(savedEncounter.id)).thenReturn(
            Single.just(savedEncounterWithExtras)
        )
        whenever(mockEncounterRepository.upsert(updatedEncounterWithExtras)).thenReturn(
            Completable.complete()
        )
        whenever(mockEncounterRepository.deleteEncounterItems(removedEncounterItemIds)).thenReturn(
            Completable.complete()
        )

        useCase.execute(updatedEncounterWithExtras).test().assertComplete()
    }
}
