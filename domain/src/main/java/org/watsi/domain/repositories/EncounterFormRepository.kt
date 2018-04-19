package org.watsi.domain.repositories

import org.watsi.domain.entities.Delta

interface EncounterFormRepository {
    fun sync(deltas: List<Delta>)
}
