package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository

class PersistReturnedEncountersUseCase(private val encounterRepository: EncounterRepository) {
    fun execute(returnedEncounters: List<EncounterWithExtras>): Completable {
        return Completable.fromCallable {
            val revisedEncounterIds = encounterRepository.revisedIds().blockingGet()
            encounterRepository.upsert(returnedEncounters.filter {
                !revisedEncounterIds.contains(it.encounter.id)
            }).blockingAwait()
        }.subscribeOn(Schedulers.io())
    }
}
