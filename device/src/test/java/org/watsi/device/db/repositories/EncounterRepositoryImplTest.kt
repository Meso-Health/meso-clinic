package org.watsi.device.db.repositories

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
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.EncounterApi
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.db.models.EncounterWithItemsModel
import org.watsi.device.factories.EncounterModelFactory
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
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
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: EncounterRepositoryImpl

    val encounterModel = EncounterModelFactory.build()
    val encounterWithItemsModel = EncounterWithItemsModel(encounterModel, emptyList())

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = EncounterRepositoryImpl(mockDao, mockApi, mockSessionManager, clock)
    }

    @Test
    fun find() {
        whenever(mockDao.find(encounterModel.id)).thenReturn(Single.just(encounterWithItemsModel))

        repository.find(encounterModel.id).test().assertValue(encounterWithItemsModel.toEncounterWithItems())
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
                encounter, listOf(encounterItemWithBillable), listOf(encounterForm))

        repository.create(encounterWithItemsAndForms, deltas).test().assertComplete()

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
