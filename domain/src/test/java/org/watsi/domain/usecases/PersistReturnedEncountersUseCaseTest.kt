package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.times
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
import org.watsi.domain.factories.EncounterWithExtrasFactory
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class PersistReturnedEncountersUseCaseTest {
    @Mock lateinit var mockEncounterRepository: EncounterRepository

    lateinit var useCase: PersistReturnedEncountersUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = PersistReturnedEncountersUseCase(mockEncounterRepository)
    }

    @Test
    fun execute() {
        val encounter1 = EncounterWithExtrasFactory.build()
        val encounter2 = EncounterWithExtrasFactory.build()
        val encounter3 = EncounterWithExtrasFactory.build()
        val encounter4 = EncounterWithExtrasFactory.build()
        val encounter5 = EncounterWithExtrasFactory.build()

        val revisedIds = (1..10).map { UUID.randomUUID() } + listOf(encounter1.encounter.id, encounter4.encounter.id)
        whenever(mockEncounterRepository.revisedIds()).thenReturn(Single.just(revisedIds))
        whenever(mockEncounterRepository.upsert(listOf(encounter2, encounter3, encounter5))).thenReturn(Completable.complete())

        useCase.execute(listOf(encounter1, encounter2, encounter3, encounter4, encounter5)).test().assertComplete()
    }
}
