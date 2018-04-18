package org.watsi.uhp.repositories

import org.watsi.domain.entities.Delta
import org.watsi.uhp.database.DatabaseHelper
import org.watsi.uhp.models.Encounter

class EncounterRepositoryImpl : EncounterRepository {

    override fun create(encounter: Encounter) {
        // TODO: set token, validate, set ID (if necessary) and persist associations
        DatabaseHelper.fetchDao(Encounter::class.java).create(encounter)
    }

    override fun sync(deltas: List<Delta>) {
        // TODO
    }
}
