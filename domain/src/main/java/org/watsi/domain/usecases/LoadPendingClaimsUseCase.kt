package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository

class LoadPendingClaimsUseCase(private val encounterRepository: EncounterRepository): LoadClaimsUseCase {
    override fun execute(): Flowable<List<EncounterWithExtras>> {
        return encounterRepository.loadPendingClaims()
    }
}
