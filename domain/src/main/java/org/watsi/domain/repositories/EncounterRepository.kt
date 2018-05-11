package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterWithItemsAndForms
import java.util.UUID

interface EncounterRepository {
    fun find(id: UUID): Single<Encounter>
    fun create(encounterWithItemsAndForms: EncounterWithItemsAndForms, deltas: List<Delta>): Completable
    fun sync(deltas: List<Delta>): Completable
}
