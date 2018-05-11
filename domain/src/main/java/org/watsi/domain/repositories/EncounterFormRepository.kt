package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterFormWithEncounterAndPhoto
import java.util.UUID

interface EncounterFormRepository {
    fun find(id: UUID): Single<EncounterFormWithEncounterAndPhoto>
    fun sync(delta: Delta): Completable
}
