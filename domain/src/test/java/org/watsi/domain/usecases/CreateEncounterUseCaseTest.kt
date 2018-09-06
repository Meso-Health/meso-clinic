package org.watsi.domain.usecases

import com.nhaarman.mockito_kotlin.whenever
import io.reactivex.Completable
import io.reactivex.Maybe
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.watsi.domain.entities.Delta
import org.watsi.domain.factories.BillableFactory
import org.watsi.domain.factories.BillableWithPriceScheduleFactory
import org.watsi.domain.factories.EncounterFactory
import org.watsi.domain.factories.EncounterFormFactory
import org.watsi.domain.factories.EncounterItemFactory
import org.watsi.domain.factories.EncounterItemWithBillableAndPriceFactory
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
    fun execute_encounterDoesNotHaveNewBillablesOrEncounterForms_createsEncounterWithDelta() {
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build()
        val encounterDelta = Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = encounterWithItemsAndForms.encounter.id
        )

        whenever(mockEncounterRepository.insert(encounterWithItemsAndForms, listOf(encounterDelta)))
                .thenReturn(Completable.complete())

        useCase.execute(encounterWithItemsAndForms).test().assertComplete()
    }

    @Test
    fun execute_encounterHasNewBillables_createsEncounterWithDeltaAndBillablesWithDeltas() {
        val encounter = EncounterFactory.build()
        val billable = BillableFactory.build()
        val encounterItem = EncounterItemFactory.build(encounterId = encounter.id, billableId = billable.id)
        val encounterItemRelation = EncounterItemWithBillableAndPriceFactory.build(
            BillableWithPriceScheduleFactory.build(billable), encounterItem
        )
        val encounterWithItemsAndForms = EncounterWithItemsAndFormsFactory.build(
            encounter = encounter,
            encounterItemRelations = listOf(encounterItemRelation)
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
        whenever(mockBillableRepository.create(billable, billableDelta))
                .thenReturn(Completable.complete())
        whenever(mockEncounterRepository.insert(encounterWithItemsAndForms, listOf(encounterDelta)))
                .thenReturn(Completable.complete())

        useCase.execute(encounterWithItemsAndForms).test().assertComplete()
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

        whenever(mockEncounterRepository.insert(encounterWithItemsAndForms, listOf(encounterDelta, encounterFormDelta)))
                .thenReturn(Completable.complete())

        useCase.execute(encounterWithItemsAndForms).test().assertComplete()
    }
}
