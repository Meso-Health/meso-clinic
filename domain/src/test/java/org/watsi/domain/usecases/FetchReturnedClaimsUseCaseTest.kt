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
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

@RunWith(MockitoJUnitRunner::class)
class FetchReturnedClaimsUseCaseTest {

    @Mock lateinit var mockEncounterRepository: EncounterRepository
    @Mock lateinit var mockPersistReturnedEncountersUseCase: PersistReturnedEncountersUseCase
    @Mock lateinit var mockMarkReturnedEncountersAsRevisedUseCase: MarkReturnedEncountersAsRevisedUseCase

    lateinit var useCase: FetchReturnedClaimsUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = FetchReturnedClaimsUseCase(mockPersistReturnedEncountersUseCase,
            mockMarkReturnedEncountersAsRevisedUseCase, mockEncounterRepository)

        whenever(mockPersistReturnedEncountersUseCase.execute(any())).thenReturn(Completable.complete())
        whenever(mockMarkReturnedEncountersAsRevisedUseCase.execute(any())).thenReturn(Completable.complete())
    }

    @Test
    fun execute_noReturnedClaims_noReturnedClaimsOnDevice() {
        mockReturnedClaimsFromFetch(emptyList())
        mockReturnedClaimsIdsOnDevice(emptyList())

        useCase.execute().test().assertComplete()

        verify(mockPersistReturnedEncountersUseCase, times(1)).execute(emptyList())
        verify(mockMarkReturnedEncountersAsRevisedUseCase, times(1)).execute(emptyList())
    }

    @Test
    fun execute_returnedClaims_noReturnedClaimsOnDevice() {
        mockReturnedClaimsIdsOnDevice(emptyList())
        val returnedEncountersFromFetch = listOf(EncounterWithExtrasFactory.build())
        mockReturnedClaimsFromFetch(returnedEncountersFromFetch)

        useCase.execute().test().assertComplete()

        verify(mockPersistReturnedEncountersUseCase, times(1)).execute(returnedEncountersFromFetch)
        verify(mockMarkReturnedEncountersAsRevisedUseCase, times(1)).execute(emptyList())
    }

    @Test
    fun execute_returnedClaims_returnedClaimsOnDevice() {
        val returnedEncountersFromFetch = listOf(EncounterWithExtrasFactory.build())
        val returnedEncountersOnDevice = listOf(
            returnedEncountersFromFetch.first().encounter.id,
            UUID.randomUUID(),
            UUID.randomUUID()
        )
        mockReturnedClaimsIdsOnDevice(returnedEncountersOnDevice)
        mockReturnedClaimsFromFetch(returnedEncountersFromFetch)

        useCase.execute().test().assertComplete()

        verify(mockPersistReturnedEncountersUseCase, times(1)).execute(returnedEncountersFromFetch)
        verify(mockMarkReturnedEncountersAsRevisedUseCase, times(1)).execute(returnedEncountersOnDevice.slice(1..2))
    }

    private fun mockReturnedClaimsIdsOnDevice(returnedClaimIds: List<UUID>) {
        whenever(mockEncounterRepository.returnedIds()).thenReturn(Single.just(returnedClaimIds))
    }

    private fun mockReturnedClaimsFromFetch(returnedClaims: List<EncounterWithExtras>) {
        whenever(mockEncounterRepository.fetchReturnedClaims()).thenReturn(Single.just(returnedClaims))
    }
}
