package org.watsi.domain.usecases

import io.reactivex.Single
import org.watsi.domain.entities.Encounter
import org.watsi.domain.repositories.EncounterRepository

class CheckForSameDayEncountersUseCase(private val encounterRepository: EncounterRepository) {

    fun execute(encounter: Encounter): Single<Boolean> {
        return encounterRepository.encountersOccurredSameDay(encounter.occurredAt, encounter.memberId)
    }

    class SameDayEncounterException: Exception()
}
