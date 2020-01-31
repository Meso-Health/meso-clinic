package org.watsi.domain.usecases

import io.reactivex.Maybe
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository

class LoadOnePendingClaimUseCase(
    private val encounterRepository: EncounterRepository
) {
    fun execute(): Maybe<EncounterWithExtras> {
        return encounterRepository.loadOnePendingClaim()
    }
}
