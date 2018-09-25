package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.mock
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
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterWithItemsAndFormsFactory
import org.watsi.domain.factories.MemberFactory

@RunWith(MockitoJUnitRunner::class)
class ReviseMemberAndClaimUseCaseTest {

    @Mock lateinit var mockCreateMemberUseCase: CreateMemberUseCase
    @Mock lateinit var mockCreateEncounterUseCase: CreateEncounterUseCase
    @Mock lateinit var mockMarkReturnedEncountersAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase
    lateinit var useCase: ReviseMemberAndClaimUseCase
    lateinit var clock: Clock

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = ReviseMemberAndClaimUseCase(mockCreateMemberUseCase, mockCreateEncounterUseCase, mockMarkReturnedEncountersAsRevisedUseCase)
        clock = Clock.systemDefaultZone()
    }


    @Test
    fun execute_encounterHasEncounterForms_submitNowTrue() {
        val encounter = EncounterFactory.build()
        val member = MemberFactory.build()
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
            encounter = encounter,
            forms = listOf(encounterForm)
        )

        whenever(mockCreateMemberUseCase.execute(mock(), true))
            .thenReturn(Completable.complete())
        whenever(mockCreateEncounterUseCase.execute(mock(), true, clock))
            .thenReturn(Completable.complete())
        whenever(mockMarkReturnedEncountersAsRevisedUseCase.execute(listOf(encounter.id)))
            .thenReturn(Completable.complete())


        useCase.execute(member, encounterWithItemsAndForms, true, clock).test().assertComplete()
    }

    @Test
    fun execute_encounterHasEncounterForms_submitNowFalse() {
        val encounter = EncounterFactory.build()
        val member = MemberFactory.build()
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
            encounter = encounter,
            forms = listOf(encounterForm)
        )

        whenever(mockCreateMemberUseCase.execute(mock(), false))
            .thenReturn(Completable.complete())
        whenever(mockCreateEncounterUseCase.execute(mock(), false, clock))
            .thenReturn(Completable.complete())
        whenever(mockMarkReturnedEncountersAsRevisedUseCase.execute(listOf(encounter.id)))
            .thenReturn(Completable.complete())


        useCase.execute(member, encounterWithItemsAndForms, false, clock).test().assertComplete()
    }
}