package org.watsi.domain.usecases

import io.reactivex.Completable
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

class DeletePendingClaimAndMemberUseCase(private val encounterRepository: EncounterRepository) {
    fun execute(encounterId: UUID): Completable {
        return encounterRepository.delete(encounterId).subscribeOn(Schedulers.io())
    }
}
