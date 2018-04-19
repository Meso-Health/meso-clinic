package org.watsi.domain.repositories

import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.Encounter

interface EncounterRepository {
    fun create(encounter: Encounter)
    fun sync(deltas: List<Delta>)
}
