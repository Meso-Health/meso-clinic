package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository

class DeletePendingClaimAndMemberUseCase(private val encounterRepository: EncounterRepository) {
    fun execute(encounterWithExtras: EncounterWithExtras): Completable {
        return encounterRepository.delete(encounterWithExtras).subscribeOn(Schedulers.io())
    }
}
