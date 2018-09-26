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
import org.watsi.domain.factories.EncounterWithItemsAndFormsFactory
import org.watsi.domain.factories.MemberFactory

@RunWith(MockitoJUnitRunner::class)
class ReviseMemberAndClaimUseCaseTest {

    @Mock lateinit var mockCreateMemberUseCase: CreateMemberUseCase
    @Mock lateinit var mockCreateEncounterUseCase: CreateEncounterUseCase
    @Mock lateinit var mockMarkReturnedEncountersAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase
    lateinit var useCase: ReviseMemberAndClaimUseCase
    lateinit var fixedClock: Clock

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = ReviseMemberAndClaimUseCase(mockCreateMemberUseCase, mockCreateEncounterUseCase, mockMarkReturnedEncountersAsRevisedUseCase)
        fixedClock = Clock.fixed(Instant.now(), ZoneId.systemDefault())
    }


    @Test
    fun execute_encounterHasEncounterForms_submitNowTrue_createsEncounterAndMemberAndMarksRevised() {
        val encounter = EncounterFactory.build()
        val member = MemberFactory.build()
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
            encounter = encounter,
            forms = listOf(encounterForm)
        )

        val encounterWithItemsAndFormsAndTimestamps = encounterWithItemsAndForms.copy(
            encounter = encounterWithItemsAndForms.encounter.copy(
                submittedAt = Instant.now(fixedClock)
            )
        )

        whenever(mockCreateMemberUseCase.execute(any(), eq(true)))
            .thenReturn(Completable.complete())
        whenever(mockCreateEncounterUseCase.execute(any(), eq(true), any()))
            .thenReturn(Completable.complete())
        whenever(mockMarkReturnedEncountersAsRevisedUseCase.execute(listOf(encounter.id)))
            .thenReturn(Completable.complete())


        useCase.execute(member, encounterWithItemsAndForms, true, fixedClock).test().assertComplete()
    }

    @Test
    fun execute_encounterHasEncounterForms_submitNowFalse_createsEncounterAndMemberAndMarksRevised() {
        val encounter = EncounterFactory.build()
        val member = MemberFactory.build()
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
            encounter = encounter,
            forms = listOf(encounterForm)
        )

        val encounterWithItemsAndFormsAndTimestamps = encounterWithItemsAndForms.copy(
            encounter = encounterWithItemsAndForms.encounter.copy(
                submittedAt = Instant.now(fixedClock)
            )
        )

        whenever(mockCreateMemberUseCase.execute(any(), eq(false)))
            .thenReturn(Completable.complete())
        whenever(mockCreateEncounterUseCase.execute(any(), eq(false), any()))
            .thenReturn(Completable.complete())
        whenever(mockMarkReturnedEncountersAsRevisedUseCase.execute(listOf(encounter.id)))
            .thenReturn(Completable.complete())


        useCase.execute(member, encounterWithItemsAndForms, false, fixedClock).test().assertComplete()
    }
}
