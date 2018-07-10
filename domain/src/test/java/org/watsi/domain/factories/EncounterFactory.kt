package org.watsi.domain.factories

import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import java.util.UUID

object EncounterFactory {

    fun build(id: UUID = UUID.randomUUID(),
              memberId: UUID = UUID.randomUUID(),
              identificationEventId: UUID? = UUID.randomUUID(),
              occurredAt: Instant = Instant.now(),
              backdatedOccurredAt: Boolean = false,
              copaymentPaid: Boolean = true,
              diagnoses: List<Int> = emptyList()) : Encounter {
        return Encounter(id = id,
                         memberId = memberId,
                         identificationEventId = identificationEventId,
                         occurredAt = occurredAt,
                         backdatedOccurredAt = backdatedOccurredAt,
                         copaymentPaid = copaymentPaid,
                         diagnoses = diagnoses)
    }
}
