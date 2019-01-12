package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.verify
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
import org.watsi.device.api.CoverageApi
import org.watsi.device.api.models.PriceScheduleApi
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.UserFactory

@RunWith(MockitoJUnitRunner::class)
class PriceScheduleRepositoryImplTest {

    @Mock lateinit var mockDao: PriceScheduleDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    val user = UserFactory.build()
    val token = AuthenticationToken("token", clock.instant(), user)
    lateinit var repository: PriceScheduleRepositoryImpl

    val priceScheduleModel = PriceScheduleModelFactory.build(clock = clock)
    val deltaModel = DeltaModelFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.PRICE_SCHEDULE,
            modelId = priceScheduleModel.id,
            synced = false,
            clock = clock
    )

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        repository = PriceScheduleRepositoryImpl(mockDao, mockApi, mockSessionManager, clock)
    }

    @Test
    fun sync_noPriceSchedule_completes() {
        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockDao.find(priceScheduleModel.id)).thenReturn(Maybe.empty())

        repository.sync(deltaModel.toDelta()).test().assertComplete()
    }

    @Test
    fun sync() {
        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockDao.find(priceScheduleModel.id)).thenReturn(Maybe.just(priceScheduleModel))
        whenever(mockApi.postPriceSchedule(token.getHeaderString(), user.providerId, PriceScheduleApi(priceScheduleModel.toPriceSchedule())))
                .thenReturn(Completable.complete())

        repository.sync(deltaModel.toDelta()).test().assertComplete()
    }

    @Test
    fun create() {
        repository.create(priceScheduleModel.toPriceSchedule(), deltaModel.toDelta()).test().assertComplete()

        verify(mockDao).insertWithDelta(priceScheduleModel, deltaModel)
    }
}
