package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import okhttp3.OkHttpClient
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.threeten.bp.ZoneId
import org.watsi.device.api.CoverageApi
import org.watsi.device.db.daos.PhotoDao
import org.watsi.device.db.models.PhotoModel
import org.watsi.device.factories.PhotoModelFactory
import org.watsi.device.managers.SessionManager
import org.watsi.domain.factories.PhotoFactory

@RunWith(MockitoJUnitRunner::class)
class PhotoRepositoryImplTest {

    @Mock lateinit var mockDao: PhotoDao
    @Mock lateinit var mockApi: CoverageApi
    @Mock lateinit var mockSessionManager: SessionManager
    @Mock lateinit var mockOkHttpClient: OkHttpClient
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: PhotoRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = PhotoRepositoryImpl(mockDao, mockApi, mockSessionManager, clock, mockOkHttpClient)
    }

    @Test
    fun find() {
        val photo = PhotoFactory.build()
        whenever(mockDao.find(photo.id)).thenReturn(Single.just(PhotoModel.fromPhoto(photo, clock)))

        repository.find(photo.id).test().assertValue(photo)
    }

    @Test
    fun create() {
        val photo = PhotoFactory.build()

        repository.create(photo).test().assertComplete()

        verify(mockDao).insert(PhotoModel.fromPhoto(photo, clock))
    }

    @Test
    fun deleteSynced() {
        val model = PhotoModelFactory.build()
        whenever(mockDao.canBeDeleted()).thenReturn(Single.just(listOf(model)))

        repository.deleteSynced().test().assertComplete()

        verify(mockDao).destroy(model)
    }
}
