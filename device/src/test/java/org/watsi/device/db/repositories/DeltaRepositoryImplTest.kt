package org.watsi.device.db.repositories

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
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.DeltaFactory

@RunWith(MockitoJUnitRunner::class)
class DeltaRepositoryImplTest {

    @Mock lateinit var mockDao: DeltaDao
    val clock = Clock.fixed(Instant.now(),  ZoneId.of("UTC"))
    lateinit var repository: DeltaRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = DeltaRepositoryImpl(mockDao, clock)
    }

    @Test
    fun unsynced() {
        val modelName = Delta.ModelName.MEMBER
        val deltaModel = DeltaModelFactory.build(modelName = modelName)
        whenever(mockDao.unsynced(modelName)).thenReturn(Single.just(listOf(deltaModel)))

        repository.unsynced(modelName).test().assertValue(listOf(deltaModel.toDelta()))
    }

    @Test
    fun markAsSynced() {
        val delta = DeltaFactory.build(synced = false)

        repository.markAsSynced(listOf(delta)).test().assertComplete()

        verify(mockDao).update(listOf(DeltaModel.fromDelta(delta.copy(synced = true), clock)))
    }
}
