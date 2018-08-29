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
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.db.models.EncounterWithItemsModel
import org.watsi.device.db.models.EncounterWithMemberAndItemsAndFormsModel
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.DiagnosisModelFactory
import org.watsi.device.factories.EncounterFormModelFactory
import org.watsi.device.factories.EncounterItemModelFactory
import org.watsi.device.factories.EncounterItemWithBillableModelFactory
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.factories.MemberModelFactory
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.UserFactory
import org.watsi.domain.relations.EncounterItemWithBillable
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

        val encounterItemModel1 = EncounterItemModelFactory.build(
            encounterId = encounterModel1.id, billableId = billableModel1.id
        )
        val encounterItemModel2 = EncounterItemModelFactory.build(
            encounterId = encounterModel1.id, billableId = billableModel2.id
        )
        val encounterItemModel3 = EncounterItemModelFactory.build(
            encounterId = encounterModel2.id, billableId = billableModel3.id
        )

        val encounterItemWithBillableModel1 =
            EncounterItemWithBillableModelFactory.build(billableModel1, encounterItemModel1)
        val encounterItemWithBillableModel2 =
            EncounterItemWithBillableModelFactory.build(billableModel2, encounterItemModel2)
        val encounterItemWithBillableModel3 =
            EncounterItemWithBillableModelFactory.build(billableModel3, encounterItemModel3)

        val encounterFormModel1 = EncounterFormModelFactory.build(encounterId = encounterModel1.id)
        val encounterFormModel2 = EncounterFormModelFactory.build(encounterId = encounterModel2.id)

        val encounterWithMemberAndItemsAndFormsModel1 = EncounterWithMemberAndItemsAndFormsModel(
            encounterModel1,
            listOf(memberModel1),
            listOf(encounterItemWithBillableModel1, encounterItemWithBillableModel2),
            listOf(encounterFormModel1)
        )
        val encounterWithMemberAndItemsAndFormsModel2 = EncounterWithMemberAndItemsAndFormsModel(
            encounterModel2,
            listOf(memberModel2),
            listOf(encounterItemWithBillableModel3),
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
            returnedEncounterWithExtras[0].encounterItems,
            listOf(
                encounterItemWithBillableModel1.toEncounterItemWithBillable(),
                encounterItemWithBillableModel2.toEncounterItemWithBillable()
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
            returnedEncounterWithExtras[1].encounterItems,
            listOf(encounterItemWithBillableModel3.toEncounterItemWithBillable())
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
        val billable = BillableFactory.build()
        val encounterItemWithBillable = EncounterItemWithBillable(encounterItem, billable)
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndForms(
                encounter, listOf(encounterItemWithBillable), listOf(encounterForm), emptyList())

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
}
