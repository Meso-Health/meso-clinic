package org.watsi.device.db.repositories

import com.google.gson.JsonObject
import com.nhaarman.mockito_kotlin.any
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
    @Mock lateinit var mockIdentificationEventDao: IdentificationEventDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: IdentificationEventRepositoryImpl

    val identificationEvent = IdentificationEventFactory.build()
    val identificationEventModel = IdentificationEventModel.fromIdentificationEvent(identificationEvent, clock)
    val addDelta = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.IDENTIFICATION_EVENT,
        modelId = identificationEventModel.id,
        synced = false
    )
    val editDelta = DeltaFactory.build(
        action = Delta.Action.EDIT,
        modelName = Delta.ModelName.IDENTIFICATION_EVENT,
        modelId = identificationEventModel.id,
        field = "dismissed",
        synced = false
    )

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = IdentificationEventRepositoryImpl(mockIdentificationEventDao, mockApi, mockSessionManager, clock)
    }

    @Test
    fun create() {
        repository.create(identificationEvent, addDelta).test().assertComplete()
        verify(mockIdentificationEventDao).insertWithDelta(identificationEventModel, DeltaModel.fromDelta(addDelta, clock))
    }

    @Test
    fun sync_post() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockIdentificationEventDao.find(identificationEventModel.id))
                .thenReturn(Single.just(identificationEventModel))
        whenever(mockApi.postIdentificationEvent(token.getHeaderString(), user.providerId,
                IdentificationEventApi(identificationEvent)))
                .thenReturn(Completable.complete())

        repository.sync(listOf(addDelta)).test().assertComplete()
        verify(mockApi).postIdentificationEvent(
            token.getHeaderString(),
            user.providerId,
            IdentificationEventApi(identificationEvent)
        )
    }

    @Test
    fun sync_patch() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockIdentificationEventDao.find(identificationEventModel.id))
                .thenReturn(Single.just(identificationEventModel))
        whenever(mockApi.patchIdentificationEvent(any(), any(), any()))
                .thenReturn(Completable.complete())

        repository.sync(listOf(editDelta)).test().assertComplete()
        verify(mockApi).patchIdentificationEvent(
            token.getHeaderString(),
            identificationEvent.id,
            JsonObject().apply {
                addProperty("id", identificationEvent.id.toString())
                addProperty("dismissed", identificationEvent.dismissed)
            }
        )
    }

    @Test
    fun dismiss() {
        repository.dismiss(identificationEvent).test().assertComplete()
        verify(mockIdentificationEventDao).upsertWithDelta(
            identificationEventModel.copy(dismissed = true),
            DeltaModel.fromDelta(editDelta, clock)
        )
    }
}
