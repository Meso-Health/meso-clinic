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
import org.watsi.device.api.models.DiagnosisApi
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.factories.DiagnosisModelFactory
import org.watsi.device.managers.PreferencesManager
import org.watsi.device.managers.SessionManager
import org.watsi.domain.factories.AuthenticationTokenFactory

@RunWith(MockitoJUnitRunner::class)
class DiagnosisRepositoryImplTest {

    @Mock lateinit var mockDao: DiagnosisDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockPreferencesManager: PreferencesManager
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: DiagnosisRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = DiagnosisRepositoryImpl(
                mockDao, mockApi, mockSessionManager, mockPreferencesManager, clock)
    }

    @Test
    fun all() {
        val diagnosesModels = listOf(DiagnosisModelFactory.build(), DiagnosisModelFactory.build())
        whenever(mockDao.all()).thenReturn(Single.just(diagnosesModels))

        repository.all().test().assertValue(diagnosesModels.map { it.toDiagnosis() })
    }

    @Test
    fun fetch_noCurrentToken_completes() {
        whenever(mockSessionManager.currentToken()).thenReturn(null)

        repository.fetch().test().assertComplete()
    }

    @Test
    fun fetch_hasToken_savesResponse() {
        val authToken = AuthenticationTokenFactory.build()
        val model = DiagnosisModelFactory.build(clock = clock)
        val apiResponse = DiagnosisApi(model.id, model.description, model.searchAliases)
        whenever(mockSessionManager.currentToken()).thenReturn(authToken)
        whenever(mockApi.diagnoses(any())).thenReturn(Single.just(listOf(apiResponse)))

        repository.fetch().test().assertComplete()

        verify(mockApi).diagnoses(authToken.getHeaderString())
        verify(mockDao).deleteNotInList(listOf(model.id))
        verify(mockDao).insert(listOf(model))
        verify(mockPreferencesManager).updateDiagnosesLastFetched(clock.instant())
    }
}
