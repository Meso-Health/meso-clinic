package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
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
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.BillableApi
import org.watsi.device.api.models.BillableWithPriceScheduleApi
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.BillableWithPriceSchedulesModelFactory
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.AuthenticationTokenFactory
import org.watsi.domain.factories.UserFactory
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class BillableRepositoryImplTest {

    @Mock lateinit var mockDao: BillableDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
    lateinit var repository: BillableRepositoryImpl

    val billableModel = BillableModelFactory.build(clock = clock)
    val deltaModel = DeltaModelFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.BILLABLE,
            modelId = billableModel.id,
            synced = false,
            clock = clock
    )

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        repository = BillableRepositoryImpl(mockDao, mockApi, mockSessionManager, mockPreferencesManager, clock)
    }

    @Test
    fun allWithPrice() {
        val models = listOf(
            BillableWithPriceSchedulesModelFactory.build(),
            BillableWithPriceSchedulesModelFactory.build()
        )
        whenever(mockDao.allWithPrice()).thenReturn(Single.just(models))

        repository.all().test().assertValue(models.map { billableWithPriceSchedulesModel ->
            billableWithPriceSchedulesModel.toBillableWithCurrentPriceSchedule()
        })
    }

    @Test
    fun ofType() {
        val type = Billable.Type.DRUG
        val models = listOf(
            BillableWithPriceSchedulesModelFactory.build(),
            BillableWithPriceSchedulesModelFactory.build()
        )
        val modelIds = models.map { it.billableModel!!.id }
        whenever(mockDao.idsOfType(type)).thenReturn(Single.just(modelIds))
        whenever(mockDao.findWithPrice(modelIds)).thenReturn(Single.just(models))

        repository.ofType(type).test().assertValue(models.map { billableWithPriceSchedulesModel ->
            billableWithPriceSchedulesModel.toBillableWithCurrentPriceSchedule()
        })
    }

    @Test
    fun ofType_moreThan1000_findsByChunk() {
        val type = Billable.Type.DRUG
        val modelIds = (1..1001).map { UUID.randomUUID() }
        whenever(mockDao.idsOfType(type)).thenReturn(Single.just(modelIds))
        whenever(mockDao.findWithPrice(any())).thenReturn(Single.just(emptyList()))

        repository.ofType(type).test().assertComplete()

        verify(mockDao, times(2)).findWithPrice(any())
    }

    @Test
    fun create() {
        repository.create(billableModel.toBillable(), deltaModel.toDelta()).test().assertComplete()

        verify(mockDao).insertWithDelta(billableModel, deltaModel)
    }

    @Test
    fun delete() {
        val ids = List(1001) { UUID.randomUUID() }

        repository.delete(ids).test().assertComplete()

        verify(mockDao).delete(ids.take(999))
        verify(mockDao).delete(ids.takeLast(2))
    }

    @Test
    fun uniqueCompositions() {
        val compositions = listOf("tablet", "vial")
        whenever(mockDao.distinctCompositions()).thenReturn(Single.just(compositions))

        repository.uniqueCompositions().test().assertValue(compositions)
    }

    @Test
    fun fetch_noCurrentAuthenticationToken_completes() {
        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(null)

        repository.fetch().test().assertComplete()
    }

    @Test
    fun fetch_hasToken_savesResponse() {
        val authToken = AuthenticationTokenFactory.build()
        val noChange = BillableModelFactory.build(clock = clock)
        val priceScheduleModelForNoChangeBillable = PriceScheduleModelFactory.build(noChange.id, clock = clock)
        val noChangeApi = BillableWithPriceScheduleApi(
            billable = noChange.toBillable(),
            activePriceSchedule = priceScheduleModelForNoChangeBillable.toPriceSchedule()
        )

        val serverEdited = BillableModelFactory.build(name = "fucap", clock = clock)
        val priceScheduleModelForServerEditedBillable = PriceScheduleModelFactory.build(serverEdited.id, clock = clock)
        val serverEditedApi = BillableWithPriceScheduleApi(
            billable = serverEdited.copy(name = "flucap").toBillable(),
            activePriceSchedule = priceScheduleModelForServerEditedBillable.toPriceSchedule()
        )

        val serverAdded = BillableModelFactory.build(clock = clock)
        val priceScheduleModelForServerAddedBillable = PriceScheduleModelFactory.build(serverAdded.id, clock = clock)
        val serverAddedApi = BillableWithPriceScheduleApi(
            billable = serverAdded.toBillable(),
            activePriceSchedule = priceScheduleModelForServerAddedBillable.toPriceSchedule()
        )

        val serverRemoved = BillableModelFactory.build(clock = clock)
        val clientAdded = BillableModelFactory.build(clock = clock)

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(authToken)
        whenever(mockApi.getBillables(any(), any())).thenReturn(Single.just(listOf(
                noChangeApi,
                serverEditedApi,
                serverAddedApi
        )))

        whenever(mockDao.all()).thenReturn(Single.just(listOf(
                noChange,
                serverEdited,
                serverRemoved,
                clientAdded
        )))
        whenever(mockDao.unsynced()).thenReturn(Single.just(listOf(
                clientAdded
        )))

        repository.fetch().test().assertComplete()

        verify(mockApi).getBillables(authToken.getHeaderString(), authToken.user.providerId)
        verify(mockDao).delete(listOf(serverRemoved.id))
        verify(mockDao).upsert(
            billableModels = listOf(
                noChange,
                serverEdited.copy(name = "flucap"),
                serverAdded
            ),
            priceScheduleModels = listOf(
                priceScheduleModelForNoChangeBillable,
                priceScheduleModelForServerEditedBillable,
                priceScheduleModelForServerAddedBillable
            )
        )
        verify(mockPreferencesManager).updateBillablesLastFetched(clock.instant())
    }

    @Test
    fun opdDefaults() {
        val defaultBillable = BillableWithPriceSchedulesModelFactory.build()
        whenever(mockDao.opdDefaults()).thenReturn(Single.just(listOf(defaultBillable)))

        repository.opdDefaults().test()
            .assertValue(listOf(defaultBillable.toBillableWithCurrentPriceSchedule()))
    }

    @Test
    fun sync() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockDao.find(billableModel.id))
                .thenReturn(Maybe.just(billableModel))
        whenever(mockApi.postBillable(token.getHeaderString(), user.providerId,
                BillableApi(billableModel.toBillable())))
                .thenReturn(Completable.complete())

        repository.sync(deltaModel.toDelta()).test().assertComplete()
    }
}
