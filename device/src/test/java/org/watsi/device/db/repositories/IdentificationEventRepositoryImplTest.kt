package org.watsi.device.db.repositories

import com.nhaarman.mockito_kotlin.verify
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
import org.watsi.device.db.daos.IdentificationEventDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.factories.IdentificationEventFactory

@RunWith(MockitoJUnitRunner::class)
class IdentificationEventRepositoryImplTest {

    @Mock lateinit var mockDao: IdentificationEventDao
    val clock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    lateinit var repository: IdentificationEventRepositoryImpl

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        repository = IdentificationEventRepositoryImpl(mockDao, clock)
    }

    @Test
    fun create() {
        val identificationEvent = IdentificationEventFactory.build()
        val delta = DeltaFactory.build(modelName = Delta.ModelName.IDENTIFICATION_EVENT)

        repository.create(identificationEvent, delta).test().assertComplete()

        verify(mockDao).insertWithDelta(
                IdentificationEventModel.fromIdentificationEvent(identificationEvent, clock),
                DeltaModel.fromDelta(delta, clock))
    }
}
