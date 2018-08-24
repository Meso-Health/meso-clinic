package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.relations.EncounterWithItems
import org.watsi.domain.relations.EncounterWithItemsAndForms
import java.util.UUID

interface EncounterRepository {
    fun find(id: UUID): Single<EncounterWithItems>
    fun find(ids: List<UUID>): Single<List<Encounter>>
    fun update(encounters: List<Encounter>): Completable
    fun insert(encounterWithItemsAndForms: EncounterWithItemsAndForms, deltas: List<Delta>): Completable
    fun upsert(encounterWithItemsAndForms: EncounterWithItemsAndForms): Completable
    fun sync(delta: Delta): Completable
    fun fetchReturnedClaims(): Single<List<EncounterWithExtras>>
    fun returnedIds(): Single<List<UUID>>
    fun revisedIds(): Single<List<UUID>>
}
