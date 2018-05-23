package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.IdentificationEventFactory
import org.watsi.domain.repositories.IdentificationEventRepository

@RunWith(MockitoJUnitRunner::class)
class CreateIdentificationEventUseCaseTest {

    @Mock lateinit var mockIdentificationEventRepository: IdentificationEventRepository
    lateinit var useCase: CreateIdentificationEventUseCase

    @Before
    fun setup() {
        useCase = CreateIdentificationEventUseCase(mockIdentificationEventRepository)
    }

    @Test
    fun execute_createsIdentificationEventAndIdentificationEventDelta() {
        val idEvent = IdentificationEventFactory.build()
        val idEventDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.IDENTIFICATION_EVENT,
                modelId = idEvent.id
        )

        useCase.execute(idEvent)

        verify(mockIdentificationEventRepository).create(idEvent, idEventDelta)
    }
}
