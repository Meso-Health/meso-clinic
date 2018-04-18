package org.watsi.uhp.repositories

import org.watsi.uhp.models.Encounter

interface EncounterRepository {
    fun create(encounter: Encounter)
}
