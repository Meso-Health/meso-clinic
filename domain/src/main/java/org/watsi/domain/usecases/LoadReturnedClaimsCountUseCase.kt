package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.repositories.EncounterRepository

class LoadReturnedClaimsCountUseCase(private val encounterRepository: EncounterRepository) {
    fun execute(): Flowable<Int> {
        return encounterRepository.loadReturnedClaimsCount()
    }
}
