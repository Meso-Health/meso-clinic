package org.watsi.device.db.repositories

import com.google.gson.JsonObject
import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Flowable
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
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.daos.IdentificationEventDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.device.factories.IdentificationEventModelFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.factories.UserFactory

@RunWith(MockitoJUnitRunner::class)
class IdentificationEventRepositoryImplTest {
    @Mock lateinit var mockIdentificationEventDao: IdentificationEventDao
    @Mock lateinit var mockEncounterDao: EncounterDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    val clock = Clock.fixed(Instant.now(), ZoneId.of("UTC"))
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

    val user = UserFactory.build()
    val token = AuthenticationToken("token", clock.instant(), user)

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = IdentificationEventRepositoryImpl(mockIdentificationEventDao, mockEncounterDao, mockApi, mockSessionManager, mockPreferencesManager, clock)
    }

    @Test
    fun create() {
        repository.create(identificationEvent, addDelta).test().assertComplete()
        verify(mockIdentificationEventDao).insertWithDelta(identificationEventModel, DeltaModel.fromDelta(addDelta, clock))
    }

    @Test
    fun fetch_noCurrentToken_completes() {
        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(null)

        repository.fetch().test().assertComplete()
    }

    @Test
    fun fetch_hasToken_succeeds_updatesIdentificationEvents() {
        val noChange = IdentificationEventModelFactory.build(clock = clock)
        val noChangeApi = IdentificationEventApi(noChange.toIdentificationEvent())
        val serverEdited = IdentificationEventModelFactory.build(clock = clock, dismissed = false)
        val serverEditedApi = IdentificationEventApi(serverEdited.copy(dismissed = true).toIdentificationEvent())
        val serverAdded = IdentificationEventModelFactory.build(clock = clock)
        val serverAddedApi = IdentificationEventApi(serverAdded.toIdentificationEvent())
        val serverRemoved = IdentificationEventModelFactory.build(clock = clock)
        val clientEdited = IdentificationEventModelFactory.build(clock = clock, dismissed = false)
        val clientEditedApi = IdentificationEventApi(clientEdited.copy(dismissed = true).toIdentificationEvent())
        val clientAdded = IdentificationEventModelFactory.build(clock = clock)
        val clientEditedServerEdited = IdentificationEventModelFactory.build(clock = clock, dismissed = false)
        val clientEditedServerEditedApi = IdentificationEventApi(clientEditedServerEdited.copy(dismissed = true).toIdentificationEvent())
        val clientEditedServerRemoved = IdentificationEventModelFactory.build(clock = clock)

        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockApi.getOpenIdentificationEvents(any(), any())).thenReturn(Single.just(listOf(
            noChangeApi,
            serverEditedApi,
            serverAddedApi,
            clientEditedApi,
            clientEditedServerEditedApi
        )))
        whenever(mockIdentificationEventDao.unsynced()).thenReturn(Single.just(listOf(
            clientEdited,
            clientAdded,
            clientEditedServerEdited,
            clientEditedServerRemoved
        )))
        whenever(mockIdentificationEventDao.all()).thenReturn(
            Flowable.just(listOf(
            noChange,
            serverEdited,
            serverRemoved,
            clientEdited,
            clientAdded,
            clientEditedServerEdited,
            clientEditedServerRemoved
        )))
        whenever(mockEncounterDao.unsynced()).thenReturn(Single.just(emptyList()))

        repository.fetch().test().assertComplete()

        verify(mockApi).getOpenIdentificationEvents(token.getHeaderString(), token.user.providerId)
        verify(mockIdentificationEventDao).delete(listOf(serverRemoved.id))
        verify(mockPreferencesManager).updateIdentificationEventsLastFetched(clock.instant())
    }

    @Test
    fun fetch_hasToken_fails_returnsError() {
        val exception = Exception()
        whenever(mockSessionManager.currentAuthenticationToken()).thenReturn(token)
        whenever(mockApi.getOpenIdentificationEvents(any(), any())).then { throw exception }

        repository.fetch().test().assertError(exception)

        verify(mockApi).getOpenIdentificationEvents(token.getHeaderString(), token.user.providerId)
        verify(mockIdentificationEventDao, never()).unsynced()
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
