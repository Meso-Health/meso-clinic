package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.never
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.RequestBody
import okio.Buffer
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.EncounterFormDao
import org.watsi.device.db.models.EncounterFormWithPhotoModel
import org.watsi.device.factories.EncounterFormModelFactory
import org.watsi.device.factories.PhotoModelFactory
import org.watsi.device.managers.SessionManager
import org.watsi.domain.entities.AuthenticationToken
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.UserFactory
import java.util.Arrays

@RunWith(MockitoJUnitRunner::class)
class EncounterFormRepositoryImplTest {

    @Mock lateinit var mockDao: EncounterFormDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: EncounterFormRepositoryImpl

    val photoModel = PhotoModelFactory.build()
    val encounterFormModel = EncounterFormModelFactory.build(photoId = photoModel.id, clock = clock)
    val encounterFormWithPhotoModel = EncounterFormWithPhotoModel(encounterFormModel, listOf(photoModel))

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = EncounterFormRepositoryImpl(mockDao, mockApi, mockSessionManager, clock)
    }

    @Test
    fun find() {
        whenever(mockDao.find(encounterFormModel.id)).thenReturn(Single.just(encounterFormWithPhotoModel))

        repository.find(encounterFormModel.id).test().assertValue(encounterFormWithPhotoModel.toEncounterFormWithPhoto())
    }

    @Test
    fun sync() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)
        val delta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER_FORM,
                modelId = encounterFormModel.id,
                synced = false
        )
        val captor = argumentCaptor<RequestBody>()

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockDao.find(encounterFormModel.id)).thenReturn(Single.just(encounterFormWithPhotoModel))
        whenever(mockApi.patchEncounterForm(
                eq(token.getHeaderString()),
                eq(encounterFormModel.encounterId),
                captor.capture()
        )).thenReturn(Completable.complete())

        repository.sync(delta).test().assertComplete()

        val requestBody = captor.firstValue
        val buffer = Buffer()
        requestBody.writeTo(buffer)
        assertTrue(Arrays.equals(photoModel.bytes, buffer.readByteArray()))
        verify(mockDao).update(encounterFormModel.copy(photoId = null))
    }

    @Test
    fun sync_fails_doesNotUpdateForm() {
        val user = UserFactory.build()
        val token = AuthenticationToken("token", clock.instant(), user)
        val delta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER_FORM,
                modelId = encounterFormModel.id,
                synced = false
        )
        val exception = Exception()

        whenever(mockSessionManager.currentToken()).thenReturn(token)
        whenever(mockDao.find(encounterFormModel.id)).thenReturn(Single.just(encounterFormWithPhotoModel))
        whenever(mockApi.patchEncounterForm(
                eq(token.getHeaderString()), eq(encounterFormModel.encounterId), any()
        )).then { throw exception }

        repository.sync(delta).test().assertError(exception)

        verify(mockDao, never()).update(encounterFormModel.copy(photoId = null))
    }
}
