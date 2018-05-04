package org.watsi.domain.repositories

import io.reactivex.Completable
import org.watsi.domain.entities.Delta

interface EncounterFormRepository {
    fun sync(deltas: List<Delta>): Completable
}
