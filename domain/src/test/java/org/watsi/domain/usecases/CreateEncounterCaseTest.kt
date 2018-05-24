package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterWithItemsAndFormsFactory
import org.watsi.domain.repositories.EncounterRepository

@RunWith(MockitoJUnitRunner::class)
class CreateEncounterCaseTest {

    @Mock lateinit var mockEncounterRepository: EncounterRepository
    lateinit var useCase: CreateEncounterUseCase

    @Before
    fun setup() {
        useCase = CreateEncounterUseCase(mockEncounterRepository)
    }

    @Test
    fun execute_encounterDoesNotHaveEncounterForms_createsEncounterDelta() {
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build()
        val encounterDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = encounterWithItemsAndForms.encounter.id
        )

        useCase.execute(encounterWithItemsAndForms)

        verify(mockEncounterRepository).create(encounterWithItemsAndForms, listOf(encounterDelta))
    }

    @Test
    fun execute_encounterHasEncounterForms_createsEncounterAndEncounterFormDeltas() {
        val encounter = EncounterFactory.build()
        val encounterForm = EncounterFormFactory.build(encounterId = encounter.id)
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
                encounter = encounter,
                forms = listOf(encounterForm)
        )
        val encounterDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = encounter.id
        )
        val encounterFormDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER_FORM,
                modelId = encounterForm.id
        )

        useCase.execute(encounterWithItemsAndForms)

        verify(mockEncounterRepository).create(encounterWithItemsAndForms, listOf(encounterDelta, encounterFormDelta))
    }
}
