package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.BillableRepository
import org.watsi.domain.repositories.EncounterRepository

class CreateEncounterUseCase(
        private val encounterRepository: EncounterRepository,
        private val billableRepository: BillableRepository
) {

    fun execute(encounterWithItemsAndForms: EncounterWithItemsAndForms): Completable {
        return Completable.fromAction {
            val encounterDeltas = mutableListOf<Delta>()
            val newBillables = mutableListOf<Billable>()

            encounterDeltas.add(Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.ENCOUNTER,
                    modelId = encounterWithItemsAndForms.encounter.id))

            encounterWithItemsAndForms.encounterForms.map { encounterForm ->
                encounterDeltas.add(Delta(
                        action = Delta.Action.ADD,
                        modelName = Delta.ModelName.ENCOUNTER_FORM,
                        modelId = encounterForm.id))
            }

            encounterWithItemsAndForms.encounterItems.map { encounterItem ->
                val billable = encounterItem.billable
                if (billableRepository.find(billable.id).blockingGet() == null) {
                    newBillables.add(billable)
                }
            }

            Completable.concat(newBillables.map { billable ->
                val billableDelta = Delta(
                        action = Delta.Action.ADD,
                        modelName = Delta.ModelName.BILLABLE,
                        modelId = billable.id
                )
                billableRepository.create(billable, billableDelta)
            }).blockingAwait()

            encounterRepository.create(encounterWithItemsAndForms, encounterDeltas).blockingAwait()
        }
    }
}
