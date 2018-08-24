package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.entities.Encounter
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

class MarkReturnedEncounterAsRevisedUseCase(private val encounterRepository: EncounterRepository) {
    fun execute(encounterIds: List<UUID>): Completable {
        return Completable.fromCallable {
            val encounters = encounterRepository.find(encounterIds).blockingGet()
            encounterRepository.update(encounters.map { encounter ->
                encounter.copy(adjudicationState = Encounter.AdjudicationState.REVISED)
            }).blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
