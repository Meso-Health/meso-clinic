package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Single
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.entities.IdentificationEvent
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.repositories.BillableRepository

@RunWith(MockitoJUnitRunner::class)
class LoadDefaultBillablesWithPriceUseCaseTest {

    @Mock lateinit var mockBillableRepo: BillableRepository
    lateinit var useCase: LoadDefaultBillablesWithPriceUseCase
    val defaultBillables = listOf(BillableWithPriceScheduleFactory.build())

    @Before
    fun setup() {
        useCase = LoadDefaultBillablesWithPriceUseCase(mockBillableRepo)
        whenever(mockBillableRepo.opdDefaultsWithPrice()).thenReturn(Single.just(defaultBillables))
    }

    @Test
    fun execute_opdIdentificationEvent_returnsDefaultOpdBillables() {
        val identificationEvent = IdentificationEventFactory.build(
                clinicNumberType = IdentificationEvent.ClinicNumberType.OPD)

        useCase.execute(identificationEvent).test().assertValue(defaultBillables)
    }

    @Test
    fun execute_deliveryIdentificationEvent_returnsEmptyList() {
        val identificationEvent = IdentificationEventFactory.build(
                clinicNumberType = IdentificationEvent.ClinicNumberType.DELIVERY)

        useCase.execute(identificationEvent).test().assertValue(emptyList())
    }
}
