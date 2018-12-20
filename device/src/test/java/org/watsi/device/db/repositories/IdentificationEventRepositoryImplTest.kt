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
import org.watsi.device.api.models.IdentificationEventApi
import org.watsi.device.db.daos.IdentificationEventDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.factories.UserFactory

@RunWith(MockitoJUnitRunner::class)
class IdentificationEventRepositoryImplTest {

    @Mock lateinit var mockDao: IdentificationEventDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: IdentificationEventRepositoryImpl

    val identificationEvent = IdentificationEventFactory.build()
    val identificationEventModel = IdentificationEventModel.fromIdentificationEvent(identificationEvent, clock)
    val delta = DeltaFactory.build(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.IDENTIFICATION_EVENT,
            modelId = identificationEventModel.id,
            synced = false
    )

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = IdentificationEventRepositoryImpl(mockDao, mockApi, mockSessionManager, clock)
    }

    @Test
    fun create() {
        repository.create(identificationEvent, delta).test().assertComplete()

        verify(mockDao).insertWithDelta(identificationEventModel, DeltaModel.fromDelta(delta, clock))
    }

    @Test
    fun sync() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockDao.find(identificationEventModel.id))
                .thenReturn(Single.just(identificationEventModel))
        whenever(mockApi.postIdentificationEvent(token.getHeaderString(), user.providerId,
                IdentificationEventApi(identificationEvent)))
                .thenReturn(Completable.complete())

        repository.sync(delta).test().assertComplete()
    }
}