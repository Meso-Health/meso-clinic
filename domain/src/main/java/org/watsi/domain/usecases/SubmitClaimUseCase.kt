package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.DeltaRepository
import org.watsi.domain.repositories.EncounterRepository

class SubmitClaimUseCase(
    private val deltaRepository: DeltaRepository,
    private val encounterRepository: EncounterRepository
) {

    fun execute(encounterWithExtras: EncounterWithExtras, clock: Clock): Completable {
        return Completable.fromAction {
            setSubmittedAt(encounterWithExtras, clock)
            createDeltas(encounterWithExtras)
        }.subscribeOn(Schedulers.io())
    }

    private fun setSubmittedAt(
        encounterWithExtras: EncounterWithExtras,
        clock: Clock
    ) {
        encounterRepository.update(
            listOf(encounterWithExtras.encounter.copy(submittedAt = Instant.now(clock)))
        ).blockingAwait()
    }

    private fun createDeltas(encounterWithExtras: EncounterWithExtras) {
        val deltas = mutableListOf<Delta>()

        deltas.add(Delta(
            action = Delta.Action.ADD,
            modelName = Delta.ModelName.ENCOUNTER,
            modelId = encounterWithExtras.encounter.id))

        encounterWithExtras.encounterForms.forEach { encounterForm ->
            deltas.add(Delta(
                action = Delta.Action.ADD,
                modelName = Delta.ModelName.ENCOUNTER_FORM,
                modelId = encounterForm.id))
        }

        deltaRepository.insert(deltas).blockingAwait()
    }
}
