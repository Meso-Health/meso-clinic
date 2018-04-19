package org.watsi.device.db.repositories

import org.threeten.bp.Clock
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.models.EncounterModel
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter
import org.watsi.domain.repositories.EncounterRepository

class EncounterRepositoryImpl(private val encounterDao: EncounterDao,
                              private val clock: Clock) : EncounterRepository {
    override fun create(encounter: Encounter) {
        encounterDao.insert(EncounterModel.fromEncounter(encounter, clock))
    }

    override fun sync(deltas: List<Delta>) {
        // TODO: implement
    }
}
