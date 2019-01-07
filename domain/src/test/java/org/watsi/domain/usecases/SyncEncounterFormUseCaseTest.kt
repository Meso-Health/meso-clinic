package org.watsi.domain.usecases

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
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterFormWithPhotoFactory
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterFormRepository

@RunWith(MockitoJUnitRunner::class)
class SyncEncounterFormFormUseCaseTest {

    @Mock lateinit var encounterFormRepo: EncounterFormRepository
    @Mock lateinit var deltaRepo: DeltaRepository
    @Mock lateinit var exception: Exception
    @Mock lateinit var onErrorCallback: (throwable: Throwable) -> Boolean
    lateinit var usecase: SyncEncounterFormUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        usecase = SyncEncounterFormUseCase(encounterFormRepo, deltaRepo)
    }

    @Test
    fun execute() {
        val syncedEncounter = EncounterFactory.build()
        val shouldBeSyncedEncounterForm = EncounterFormFactory.build(encounterId = syncedEncounter.id)
        val shouldBeSyncedEncounterFormDelta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER_FORM,
                modelId = shouldBeSyncedEncounterForm.id,
                synced = false
        )

        val unsyncedEncounter = EncounterFactory.build()
        val shouldNotBeSyncedEncounterForm = EncounterFormFactory.build(encounterId = unsyncedEncounter.id)
        val shouldNotBeSyncedEncounterFormDelta = DeltaFactory.build(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER_FORM,
                modelId = shouldNotBeSyncedEncounterForm.id,
                synced = false
        )

        whenever(deltaRepo.unsyncedModelIds(Delta.ModelName.ENCOUNTER, Delta.Action.ADD))
                .thenReturn(Single.just(listOf(unsyncedEncounter.id)))
        whenever(deltaRepo.unsynced(Delta.ModelName.ENCOUNTER_FORM))
                .thenReturn(Single.just(listOf(shouldBeSyncedEncounterFormDelta, shouldNotBeSyncedEncounterFormDelta)))
        whenever(encounterFormRepo.find(shouldBeSyncedEncounterForm.id))
                .thenReturn(Single.just(EncounterFormWithPhotoFactory.build(encounterForm = shouldBeSyncedEncounterForm)))
        whenever(encounterFormRepo.find(shouldNotBeSyncedEncounterForm.id))
                .thenReturn(Single.just(EncounterFormWithPhotoFactory.build(encounterForm = shouldNotBeSyncedEncounterForm)))
        whenever(encounterFormRepo.sync(shouldBeSyncedEncounterFormDelta))
                .thenReturn(Completable.complete())
        whenever(deltaRepo.markAsSynced(listOf(shouldBeSyncedEncounterFormDelta)))
                .thenReturn(Completable.complete())

        usecase.execute(onErrorCallback).test().assertComplete()
    }
}
