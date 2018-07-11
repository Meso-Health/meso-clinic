package org.watsi.domain.entities

import org.threeten.bp.Instant
import java.io.Serializable
import java.util.UUID

data class Encounter(val id: UUID,
                     val memberId: UUID,
                     val identificationEventId: UUID?,
                     val occurredAt: Instant,
                     val backdatedOccurredAt: Boolean = false,
                     val copaymentPaid: Boolean? = true,
                     val diagnoses: List<Int> = emptyList()) : Serializable
