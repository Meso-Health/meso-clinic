package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
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
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.factories.AuthenticationTokenFactory

@RunWith(MockitoJUnitRunner::class)
class BillableRepositoryImplTest {

    @Mock lateinit var mockDao: BillableDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager

    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: BillableRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        repository = BillableRepositoryImpl(
                mockDao, mockApi, mockSessionManager, mockPreferencesManager, clock)
    }

    @Test
    fun all() {
        val models = listOf(BillableModelFactory.build(), BillableModelFactory.build())
        whenever(mockDao.all()).thenReturn(Single.just(models))

        repository.all().test().assertValue(models.map { it.toBillable() })
    }

    @Test
    fun create() {
        val model = BillableModelFactory.build(clock = clock)

        repository.create(model.toBillable()).test().assertComplete()

        verify(mockDao).insert(model)
    }

    @Test
    fun uniqueCompositions() {
        val compositions = listOf("tablet", "vial")
        whenever(mockDao.distinctCompositions()).thenReturn(Single.just(compositions))

        repository.uniqueCompositions().test().assertValue(compositions)
    }

    @Test
    fun fetch_noCurrentToken_completes() {
        whenever(mockSessionManager.currentToken()).thenReturn(null)

        repository.fetch().test().assertComplete()
    }

    @Test
    fun fetch_hasToken_savesResponse() {
        val authToken = AuthenticationTokenFactory.build()
        val fetchedModel = BillableModelFactory.build(clock = clock)
        val unsyncedModel = BillableModelFactory.build()
        val apiResponse = BillableApi(
                fetchedModel.id, fetchedModel.type.toString(), fetchedModel.composition,
                fetchedModel.unit, fetchedModel.price, fetchedModel.name)
        whenever(mockSessionManager.currentToken()).thenReturn(authToken)
        whenever(mockApi.billables(any(), any())).thenReturn(Single.just(listOf(apiResponse)))
        whenever(mockDao.unsynced()).thenReturn(Single.just(listOf(unsyncedModel)))

        repository.fetch().test().assertComplete()

        verify(mockApi).billables(authToken.getHeaderString(), authToken.user.providerId)
        verify(mockDao).deleteNotInList(listOf(fetchedModel.id, unsyncedModel.id))
        verify(mockDao).insert(listOf(fetchedModel))
        verify(mockPreferencesManager).updateBillablesLastFetched(clock.instant())
    }
}
