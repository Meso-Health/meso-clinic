package org.watsi.domain.entities

import org.threeten.bp.Instant
import java.util.UUID

data class Encounter(val id: UUID,
                     val memberId: UUID,
                     val identificationEventId: UUID,
                     val occurredAt: Instant,
                     val backdatedOccurredAt: Instant?,
                     val copaymentPaid: Boolean)
