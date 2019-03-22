package org.watsi.device.api.models

import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Referral
import java.util.UUID

data class ReferralApi(
    val id: UUID,
    val receivingFacility: String,
    val reason: Referral.Reason,
    val encounterId: UUID,
    val number: String?,
    val date: LocalDate
) {
    constructor (referral: Referral) : this(
        id = referral.id,
        receivingFacility = referral.receivingFacility,
        reason = referral.reason,
        encounterId = referral.encounterId,
        number = referral.number,
        date = referral.date
    )

    fun toReferral(): Referral {
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
