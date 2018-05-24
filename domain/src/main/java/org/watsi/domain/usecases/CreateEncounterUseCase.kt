package org.watsi.domain.usecases

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.EncounterRepository

class CreateEncounterUseCase(private val encounterRepository: EncounterRepository) {

    fun execute(encounterWithItemsAndForms: EncounterWithItemsAndForms): Completable {
        val deltas = mutableListOf<Delta>()

        deltas.add(Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER,
                modelId = encounterWithItemsAndForms.encounter.id))

        encounterWithItemsAndForms.encounterForms.map { encounterForm ->
            deltas.add(Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.ENCOUNTER_FORM,
                    modelId = encounterForm.id))
        }

        return encounterRepository.create(encounterWithItemsAndForms, deltas)
    }
}
