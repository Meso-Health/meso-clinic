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

        if (encounterWithItemsAndForms.encounterForms.isNotEmpty()) {
            // use encounter ID in encounter form delta because it allows a more simple pattern
            // for querying the delta and creating the sync request
            deltas.add(Delta(
                    action = Delta.Action.ADD,
                    modelName = Delta.ModelName.ENCOUNTER_FORM,
                    modelId = encounterWithItemsAndForms.encounter.id))
        }

        return encounterRepository.create(encounterWithItemsAndForms, deltas)
    }
}
