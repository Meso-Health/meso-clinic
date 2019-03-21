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
import org.watsi.domain.entities.Encounter
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class MarkReturnedEncountersAsRevisedUseCaseTest {
    @Mock lateinit var mockEncounterRepository: EncounterRepository

    lateinit var useCase: MarkReturnedEncountersAsRevisedUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }

        useCase = MarkReturnedEncountersAsRevisedUseCase(mockEncounterRepository)
    }

    @Test
    fun execute() {
        val encounters = (0..3).map { EncounterFactory.build() }
        val encounterIds = encounters.map { it.id }
        whenever(mockEncounterRepository.findAll(any<List<UUID>>())).thenReturn(Single.just(encounters))
        whenever(mockEncounterRepository.update(any())).thenReturn(Completable.complete())

        useCase.execute(encounterIds).test().assertComplete()

        verify(mockEncounterRepository, times(1)).update(
            encounters.map {
                it.copy(
                    adjudicationState = Encounter.AdjudicationState.REVISED
                )
            }
        )
    }
}
