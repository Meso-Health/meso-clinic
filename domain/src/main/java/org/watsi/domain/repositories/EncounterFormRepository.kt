package org.watsi.domain.repositories

import io.reactivex.Completable
import io.reactivex.Single
import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterFormWithPhoto
import java.util.UUID

interface EncounterFormRepository {
    fun find(id: UUID): Single<EncounterFormWithPhoto>
    fun sync(delta: Delta): Completable
}
