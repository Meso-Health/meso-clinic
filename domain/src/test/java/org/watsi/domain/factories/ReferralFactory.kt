package org.watsi.domain.factories

import org.watsi.domain.entities.Referral
import java.util.UUID

object ReferralFactory {
    fun build(
        id: UUID = UUID.randomUUID(),
        encounterId: UUID = UUID.randomUUID(),
        receivingFacility: String = "Wukro General",
        reason: String = "Random reason",
        number: String? = "Number"
    ) : Referral {
        return Referral(
            id = id,
            receivingFacility = receivingFacility,
            reason = reason,
            encounterId = encounterId,
            number = number
        )
    }
}
