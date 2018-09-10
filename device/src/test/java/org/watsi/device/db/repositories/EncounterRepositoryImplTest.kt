package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.db.models.EncounterWithItemsModel
import org.watsi.device.db.models.EncounterWithMemberAndItemsAndFormsModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PriceScheduleModel
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.DiagnosisModelFactory
import org.watsi.device.factories.EncounterFormModelFactory
import org.watsi.device.factories.EncounterItemModelFactory
import org.watsi.device.factories.EncounterItemWithBillableAndPriceModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.device.factories.PriceScheduleWithBillableModelFactory
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterWithExtrasFactory
import org.watsi.domain.factories.UserFactory
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithItemsAndForms

@RunWith(MockitoJUnitRunner::class)
class EncounterRepositoryImplTest {

    @Mock lateinit var mockDao: EncounterDao
    @Mock lateinit var mockDiagnosisDao: DiagnosisDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: EncounterRepositoryImpl

    val encounterModel = EncounterModelFactory.build()
    val encounterWithItemsModel = EncounterWithItemsModel(encounterModel, emptyList())

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = EncounterRepositoryImpl(mockDao, mockDiagnosisDao, mockApi, mockSessionManager, clock)
    }

    @Test
    fun find() {
        whenever(mockDao.find(encounterModel.id)).thenReturn(Single.just(encounterWithItemsModel))

        repository.find(encounterModel.id).test().assertValue(encounterWithItemsModel.toEncounterWithItems())
    }

    @Test
    fun loadReturnedClaims() {
        val memberModel1 = MemberModelFactory.build()
        val memberModel2 = MemberModelFactory.build()

        val diagnosisModel1 = DiagnosisModelFactory.build()
        val diagnosisModel2 = DiagnosisModelFactory.build()
        val diagnosisModel3 = DiagnosisModelFactory.build()
        val diagnosesModelList1 = listOf(diagnosisModel1, diagnosisModel2)
        val diagnosesModelList2 = listOf(diagnosisModel3)
        val diagnosesIdList1 = diagnosesModelList1.map { it.id }
        val diagnosesIdList2 = diagnosesModelList2.map { it.id }
        val diagnosesList1 = diagnosesModelList1.map { it.toDiagnosis() }
        val diagnosesList2 = diagnosesModelList2.map { it.toDiagnosis() }

        val encounterModel1 = EncounterModelFactory.build(
            memberId = memberModel1.id,
            diagnoses = diagnosesIdList1,
            adjudicationState = Encounter.AdjudicationState.RETURNED
        )
        val encounterModel2 = EncounterModelFactory.build(
            memberId = memberModel2.id,
            diagnoses = diagnosesIdList2,
            adjudicationState = Encounter.AdjudicationState.RETURNED
        )

        val billableModel1 = BillableModelFactory.build()
        val billableModel2 = BillableModelFactory.build()
        val billableModel3 = BillableModelFactory.build()


        val priceScheduleModel1 = PriceScheduleModelFactory.build(billableId = billableModel1.id)
        val priceScheduleModel2 = PriceScheduleModelFactory.build(billableId = billableModel2.id)
        val priceScheduleModel3 = PriceScheduleModelFactory.build(billableId = billableModel3.id)

        val encounterItemModel1 = EncounterItemModelFactory.build(
            encounterId = encounterModel1.id,
            billableId = billableModel1.id,
            priceScheduleId = priceScheduleModel1.id
        )
        val encounterItemModel2 = EncounterItemModelFactory.build(
            encounterId = encounterModel1.id,
            billableId = billableModel2.id,
            priceScheduleId = priceScheduleModel2.id
        )
        val encounterItemModel3 = EncounterItemModelFactory.build(
            encounterId = encounterModel2.id,
            billableId = billableModel3.id,
            priceScheduleId = priceScheduleModel3.id
        )

        val encounterItemRelationModel1 =
            EncounterItemWithBillableAndPriceModelFactory.build(
                PriceScheduleWithBillableModelFactory.build(billableModel1, priceScheduleModel1),
                encounterItemModel1
            )
        val encounterItemRelationModel2 =
            EncounterItemWithBillableAndPriceModelFactory.build(
                PriceScheduleWithBillableModelFactory.build(billableModel2, priceScheduleModel2),
                encounterItemModel2
            )
        val encounterItemRelationModel3 =
            EncounterItemWithBillableAndPriceModelFactory.build(
                PriceScheduleWithBillableModelFactory.build(billableModel3, priceScheduleModel3),
                encounterItemModel3
            )

        val encounterFormModel1 = EncounterFormModelFactory.build(encounterId = encounterModel1.id)
        val encounterFormModel2 = EncounterFormModelFactory.build(encounterId = encounterModel2.id)

        val encounterWithMemberAndItemsAndFormsModel1 = EncounterWithMemberAndItemsAndFormsModel(
            encounterModel1,
            listOf(memberModel1),
            listOf(encounterItemRelationModel1, encounterItemRelationModel2),
            listOf(encounterFormModel1)
        )
        val encounterWithMemberAndItemsAndFormsModel2 = EncounterWithMemberAndItemsAndFormsModel(
            encounterModel2,
            listOf(memberModel2),
            listOf(encounterItemRelationModel3),
            listOf(encounterFormModel2)
        )

        whenever(mockDiagnosisDao.findAll(diagnosesIdList1)).thenReturn(Single.just(diagnosesModelList1))
        whenever(mockDiagnosisDao.findAll(diagnosesIdList2)).thenReturn(Single.just(diagnosesModelList2))
        whenever(mockDao.returned()).thenReturn(Flowable.fromArray(
            listOf(encounterWithMemberAndItemsAndFormsModel1, encounterWithMemberAndItemsAndFormsModel2))
        )

        val returnedEncounterWithExtras = repository.loadReturnedClaims().test().values().first()


        assertEquals(
            returnedEncounterWithExtras[0].encounter,
            encounterModel1.toEncounter()
        )
        assertEquals(
            returnedEncounterWithExtras[0].member,
            memberModel1.toMember()
        )
        assertEquals(
            returnedEncounterWithExtras[0].encounterItemRelations,
            listOf(
                encounterItemRelationModel1.toEncounterItemWithBillableAndPrice(),
                encounterItemRelationModel2.toEncounterItemWithBillableAndPrice()
            )
        )
        assertEquals(
            returnedEncounterWithExtras[0].diagnoses,
            diagnosesList1
        )
        assertEquals(
            returnedEncounterWithExtras[0].encounterForms,
            listOf(encounterFormModel1.toEncounterForm())
        )

        assertEquals(
            returnedEncounterWithExtras[1].encounter,
            encounterModel2.toEncounter()
        )
        assertEquals(
            returnedEncounterWithExtras[1].member,
            memberModel2.toMember()
        )
        assertEquals(
            returnedEncounterWithExtras[1].encounterItemRelations,
            listOf(encounterItemRelationModel3.toEncounterItemWithBillableAndPrice())
        )
        assertEquals(
            returnedEncounterWithExtras[1].diagnoses,
            diagnosesList2
        )
        assertEquals(
            returnedEncounterWithExtras[1].encounterForms,
            listOf(encounterFormModel2.toEncounterForm())
        )

    }

    @Test
    fun create() {
        val deltas = listOf(DeltaFactory.build(modelName = Delta.ModelName.ENCOUNTER))
        val encounter = EncounterFactory.build()
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id)
        val billableWithPrice = BillableWithPriceScheduleFactory.build()
        val encounterItemRelation = EncounterItemWithBillableAndPrice(encounterItem, billableWithPrice)
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndForms(
                encounter, listOf(encounterItemRelation), listOf(encounterForm), emptyList())

        repository.insert(encounterWithItemsAndForms, deltas).test().assertComplete()

        verify(mockDao).insert(
                encounter = EncounterModel.fromEncounter(encounter, clock),
                items = listOf(EncounterItemModel.fromEncounterItem(encounterItem, clock)),
                createdBillables = emptyList(),
                forms = listOf(EncounterFormModel.fromEncounterForm(encounterForm, clock)),
                deltas = deltas.map { DeltaModel.fromDelta(it, clock) }
        )
    }

    @Test
    fun sync() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)
        val delta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = encounterModel.id,
                synced = false
        )

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockDao.find(encounterModel.id)).thenReturn(Single.just(encounterWithItemsModel))
        whenever(mockApi.postEncounter(token.getHeaderString(), user.providerId,
                EncounterApi(encounterWithItemsModel.toEncounterWithItems())))
                .thenReturn(Completable.complete())

        repository.sync(delta).test().assertComplete()
    }

    @Test
    fun upsert() {
        val encounters = (1..10).map {
            EncounterWithExtrasFactory.build()
        }

        repository.upsert(encounters).test().assertComplete()

        verify(mockDao).upsert(
            encounterModels = encounters.map {
                EncounterModel.fromEncounter(it.encounter, clock)
            },
            encounterItemModels = encounters.map {
                it.encounterItemRelations.map {
                    EncounterItemModel.fromEncounterItem(it.encounterItem, clock)
                }
            }.flatten(),
            billableModels = encounters.map {
                it.encounterItemRelations.map {
                    BillableModel.fromBillable(it.billableWithPriceSchedule.billable, clock)
                }
            }.flatten(),
            priceScheduleModels = encounters.map {
                it.encounterItemRelations.map {
                    it.billableWithPriceSchedule.priceSchedules().map {
                        PriceScheduleModel.fromPriceSchedule(it, clock)
                    }
                }.flatten()
            }.flatten(),
            memberModels = encounters.map {
                MemberModel.fromMember(it.member, clock)
            }
        )
    }
}
