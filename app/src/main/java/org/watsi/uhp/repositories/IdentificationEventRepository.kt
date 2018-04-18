package org.watsi.uhp.repositories

import org.watsi.uhp.models.IdentificationEvent
import java.util.UUID

interface IdentificationEventRepository {
    fun openCheckIn(memberId: UUID): IdentificationEvent?
}
