package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import io.reactivex.plugins.RxJavaPlugins
import io.reactivex.schedulers.Schedulers
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.DiagnosisRepository

@RunWith(MockitoJUnitRunner::class)
class ValidateDiagnosesAndBillablesExistenceUseCaseTest {

    @Mock lateinit var billablesRepository: BillableRepository
    @Mock lateinit var diagnosisRepository: DiagnosisRepository

    lateinit var useCase: ValidateDiagnosesAndBillablesExistenceUseCase

    @Before
    fun setup() {
        RxJavaPlugins.setIoSchedulerHandler { Schedulers.trampoline() }
        useCase = ValidateDiagnosesAndBillablesExistenceUseCase(billablesRepository, diagnosisRepository)
    }

    @Test
    fun execute_noBillablesAndNoDiagnoses_false() {
        whenever(billablesRepository.countActive()).thenReturn(Single.just(0))
        whenever(diagnosisRepository.count()).thenReturn(Single.just(0))

        useCase.execute().test().assertError(ValidateDiagnosesAndBillablesExistenceUseCase.BillableAndDiagnosesMissingException::class.java)
    }

    @Test
    fun execute_noBillablesAndSomeDiagnoses_false() {
        whenever(billablesRepository.countActive()).thenReturn(Single.just(5))
        whenever(diagnosisRepository.count()).thenReturn(Single.just(0))

        useCase.execute().test().assertError(ValidateDiagnosesAndBillablesExistenceUseCase.BillableAndDiagnosesMissingException::class.java)
    }

    @Test
    fun execute_someBillablesAndNoDiagnoses_false() {
        whenever(billablesRepository.countActive()).thenReturn(Single.just(1))
        whenever(diagnosisRepository.count()).thenReturn(Single.just(0))

        useCase.execute().test().assertError(ValidateDiagnosesAndBillablesExistenceUseCase.BillableAndDiagnosesMissingException::class.java)
    }

    @Test
    fun execute_someBillablesAndSomeDiagnoses_true() {
        whenever(billablesRepository.countActive()).thenReturn(Single.just(1))
        whenever(diagnosisRepository.count()).thenReturn(Single.just(1))

        useCase.execute().test().assertComplete()
    }
}
