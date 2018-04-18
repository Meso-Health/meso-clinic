package org.watsi.uhp.repositories

import org.watsi.domain.entities.Delta
import org.watsi.uhp.models.IdentificationEvent
import java.util.UUID

interface IdentificationEventRepository {
    fun create(identificationEvent: IdentificationEvent)
    fun update(identificationEvent: IdentificationEvent)
    fun openCheckIn(memberId: UUID): IdentificationEvent?
    fun sync(deltas: List<Delta>)
}
