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
import org.watsi.domain.repositories.MainRepository


@RunWith(MockitoJUnitRunner::class)
class DeleteUserDataUseCaseTest {
    @Mock lateinit var mainRepository: MainRepository

    lateinit var useCase: DeleteUserDataUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = DeleteUserDataUseCase(mainRepository)
    }

    @Test
    fun execute() {
        whenever(mainRepository.deleteAllUserData())
                .thenReturn(Completable.complete())

        useCase.execute().test().assertComplete()
    }
}
