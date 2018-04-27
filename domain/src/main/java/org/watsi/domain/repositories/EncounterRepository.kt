package org.watsi.domain.repositories

import org.watsi.domain.entities.Delta
import org.watsi.domain.relations.EncounterWithItemsAndForms

interface EncounterRepository {
    fun create(encounterWithItemsAndForms: EncounterWithItemsAndForms)
    fun sync(deltas: List<Delta>)
}
