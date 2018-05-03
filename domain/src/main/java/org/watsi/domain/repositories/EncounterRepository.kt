package org.watsi.domain.repositories

import io.reactivex.Completable
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterWithItemsAndForms

interface EncounterRepository {
    fun create(encounterWithItemsAndForms: EncounterWithItemsAndForms, deltas: List<Delta>): Completable
    fun sync(deltas: List<Delta>): Completable
}
