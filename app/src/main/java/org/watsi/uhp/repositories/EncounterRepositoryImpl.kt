package org.watsi.uhp.repositories

import org.watsi.uhp.models.Encounter

class EncounterRepositoryImpl : EncounterRepository {
    override fun create(encounter: Encounter) {
        encounter.create()
    }
}
