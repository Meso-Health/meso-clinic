package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableFactory
import org.watsi.domain.factories.EncounterWithItemsAndFormsFactory
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.EncounterRepository

@RunWith(MockitoJUnitRunner::class)
class CreateEncounterUseCaseTest {

    @Mock lateinit var mockEncounterRepository: EncounterRepository
    @Mock lateinit var mockBillableRepository: BillableRepository
    lateinit var useCase: CreateEncounterUseCase

    @Before
    fun setup() {
        useCase = CreateEncounterUseCase(mockEncounterRepository, mockBillableRepository)
    }

    @Test
    fun execute_encounterDoesNotHaveNewBillablesOrEncounterForms_createsEncounterWithDelta
            () {
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
    fun execute_encounterHasNewBillables_createsEncounterWithDeltaAndBillablesWithDeltas() {
        val encounter = EncounterFactory.build()
        val billable = BillableFactory.build()
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id, billableId = billable.id)
        val encounterItemWithBillable = EncounterItemWithBillableFactory.build(billable, encounterItem)
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
                encounter = encounter,
                items = listOf(encounterItemWithBillable)
        )
        val encounterDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = encounter.id
        )
        val billableDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.BILLABLE,
                modelId = billable.id
        )

        whenever(mockBillableRepository.find(billable.id)).thenReturn(Maybe.empty())

        useCase.execute(encounterWithItemsAndForms)

        verify(mockBillableRepository).create(billable, billableDelta)
        verify(mockEncounterRepository).create(encounterWithItemsAndForms, listOf(encounterDelta))
    }

    @Test
    fun execute_encounterHasEncounterForms_createsEncounterWithDeltaAndEncounterFormDeltas() {
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
