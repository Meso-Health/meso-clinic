package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.IdentificationEventRepository
import org.watsi.domain.repositories.MemberRepository
import org.watsi.domain.repositories.PriceScheduleRepository


@RunWith(MockitoJUnitRunner::class)
class DeleteDataUseCaseTest {
    @Mock lateinit var mockBillableRepository: BillableRepository
    @Mock lateinit var mockDeltaRepository: DeltaRepository
    @Mock lateinit var mockIdentificationEventRepository: IdentificationEventRepository
    @Mock lateinit var mockMemberRepository: MemberRepository
    @Mock lateinit var mockPriceScheduleRepository: PriceScheduleRepository
    @Mock lateinit var exception: IllegalStateException

    lateinit var useCase: DeleteUserDataUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = DeleteUserDataUseCase(
            mockBillableRepository,
            mockDeltaRepository,
            mockIdentificationEventRepository,
            mockMemberRepository,
            mockPriceScheduleRepository
        )
    }

    @Test
    fun execute() {
        whenever(mockBillableRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockDeltaRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockIdentificationEventRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockMemberRepository.deleteAll())
                .thenReturn(Completable.complete())
        whenever(mockPriceScheduleRepository.deleteAll())
                .thenReturn(Completable.complete())

        useCase.execute().test().assertComplete()
    }
}
