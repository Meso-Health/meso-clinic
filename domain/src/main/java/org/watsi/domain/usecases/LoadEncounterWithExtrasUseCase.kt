package org.watsi.domain.usecases

import io.reactivex.Single
import io.reactivex.schedulers.Schedulers
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.repositories.EncounterRepository
import java.util.UUID

class LoadEncounterWithExtrasUseCase(private val encounterRepository: EncounterRepository) {
    fun execute(id: UUID): Single<EncounterWithExtras> {
        return encounterRepository.findWithExtras(id).subscribeOn(Schedulers.io())
    }
}
