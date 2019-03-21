package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.any
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
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
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterWithExtrasFactory

@RunWith(MockitoJUnitRunner::class)
class ReviseClaimUseCaseTest {

    @Mock lateinit var mockCreateEncounterUseCase: CreateEncounterUseCase
    @Mock lateinit var mockMarkReturnedEncountersAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase
    lateinit var useCase: ReviseClaimUseCase
    lateinit var fixedClock: Clock

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = ReviseClaimUseCase(mockCreateEncounterUseCase, mockMarkReturnedEncountersAsRevisedUseCase)
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    }

    @Test
    fun execute_encounterHasEncounterForms_submitNowTrue_createsEncounterAndMemberAndMarksRevised() {
        val encounter = EncounterFactory.build()
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithExtras = EncounterWithExtrasFactory.build(
            encounter = encounter,
            encounterForms = listOf(encounterForm)
        )

        whenever(mockCreateEncounterUseCase.execute(any(), eq(true), any()))
            .thenReturn(Completable.complete())
        whenever(mockMarkReturnedEncountersAsRevisedUseCase.execute(listOf(encounter.id)))
            .thenReturn(Completable.complete())


        useCase.execute(encounterWithExtras, fixedClock).test().assertComplete()
    }
}
