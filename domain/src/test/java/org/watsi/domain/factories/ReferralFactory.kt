package org.watsi.domain.factories

import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Referral
import java.util.UUID

object ReferralFactory {
    fun build(
        id: UUID = UUID.randomUUID(),
        encounterId: UUID = UUID.randomUUID(),
        receivingFacility: String = "Wukro General",
        reason: Referral.Reason = Referral.Reason.DRUG_STOCKOUT,
        number: String? = "Number",
        date: LocalDate = LocalDate.of(1993, 5, 11)
    ) : Referral {
        return Referral(
            id = id,
            receivingFacility = receivingFacility,
            reason = reason,
            encounterId = encounterId,
            number = number,
            date = date
        )
    }
}
