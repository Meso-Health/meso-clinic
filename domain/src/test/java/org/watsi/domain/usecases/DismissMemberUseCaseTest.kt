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
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.repositories.IdentificationEventRepository

@RunWith(MockitoJUnitRunner::class)
class DismissMemberUseCaseTest {
    @Mock lateinit var mockIdentificationEventRepository: IdentificationEventRepository
    @Mock lateinit var exception: IllegalStateException

    lateinit var useCase: DismissMemberUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = DismissMemberUseCase(mockIdentificationEventRepository)
        whenever(mockIdentificationEventRepository.dismiss(any())).thenReturn(Completable.complete())
    }

    @Test
    fun execute() {
        val identificationEvent = IdentificationEventFactory.build()
        whenever(mockIdentificationEventRepository.find(identificationEvent.id)).thenReturn(Single.just(identificationEvent))
        useCase.execute(identificationEvent.id).test().assertComplete()

        verify(mockIdentificationEventRepository).dismiss(identificationEvent)
    }
}
