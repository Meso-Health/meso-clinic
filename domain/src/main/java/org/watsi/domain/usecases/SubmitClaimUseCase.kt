package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterWithItemsAndForms
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository

class SubmitClaimUseCase(
    private val deltaRepository: DeltaRepository,
    private val encounterRepository: EncounterRepository
) {

    fun execute(encounterWithItemsAndForms: EncounterWithItemsAndForms, clock: Clock): Completable {
        return Completable.fromAction {
            updateEncounterTimestamp(encounterWithItemsAndForms, clock)
            createDeltas(encounterWithItemsAndForms)
        }.subscribeOn(Schedulers.io())
    }

    private fun updateEncounterTimestamp(
        encounterWithItemsAndForms: EncounterWithItemsAndForms,
        clock: Clock
    ) {
        encounterRepository.update(
            listOf(encounterWithItemsAndForms.encounter.copy(submittedAt = Instant.now(clock)))
        ).blockingAwait()
    }

    private fun createDeltas(
        encounterWithItemsAndForms: EncounterWithItemsAndForms
    ) {
        val deltas = mutableListOf<Delta>()

        deltas.add(Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = encounterWithItemsAndForms.encounter.id))

        encounterWithItemsAndForms.encounterForms.forEach { encounterForm ->
            deltas.add(Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER_FORM,
                modelId = encounterForm.id))
        }

        deltaRepository.insert(deltas).blockingAwait()
    }
}
