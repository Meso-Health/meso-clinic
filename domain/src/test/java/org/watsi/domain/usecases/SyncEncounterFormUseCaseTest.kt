package org.watsi.domain.usecases

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
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.DeltaFactory
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterFormRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class SyncEncounterFormFormUseCaseTest {

    @Mock lateinit var encounterFormRepo: EncounterFormRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    @Mock lateinit var exception: Exception
    @Mock lateinit var onErrorCallback: (throwable: Throwable) -> Boolean
    lateinit var useCase: SyncEncounterFormUseCase

    val delta = DeltaFactory.build(
        action = Delta.Action.ADD,
        modelName = Delta.ModelName.ENCOUNTER_FORM,
        modelId = UUID.randomUUID(),
        synced = false
    )

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        whenever(deltaRepo.unsynced(Delta.ModelName.ENCOUNTER_FORM)).thenReturn(Single.just(listOf(delta)))
        whenever(onErrorCallback.invoke(any())).thenReturn(true)
        useCase = SyncEncounterFormUseCase(encounterFormRepo, deltaRepo)
    }

    @Test
    fun execute_success() {
        whenever(encounterFormRepo.sync(delta)).thenReturn(Completable.complete())
        whenever(deltaRepo.markAsSynced(listOf(delta))).thenReturn(Completable.complete())
        useCase.execute(onErrorCallback).test().assertComplete()
    }

    @Test
    fun execute_failure() {
        whenever(encounterFormRepo.sync(delta)).thenReturn(Completable.error(exception))
        useCase.execute(onErrorCallback).test().assertComplete()
        verify(onErrorCallback).invoke(exception)
    }
}
