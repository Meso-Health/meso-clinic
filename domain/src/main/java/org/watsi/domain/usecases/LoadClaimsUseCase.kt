package org.watsi.domain.usecases

import io.reactivex.Flowable
import org.watsi.domain.relations.EncounterWithExtras

interface LoadClaimsUseCase {
    fun execute(): Flowable<List<EncounterWithExtras>>
}
