package org.watsi.device.api.models

import com.google.gson.Gson
import com.google.gson.JsonArray
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.Referral
import org.watsi.domain.relations.EncounterWithExtras
import java.util.UUID

data class EncounterApi(
    val id: UUID,
    val memberId: UUID,
    val identificationEventId: UUID?,
    val occurredAt: Instant,
    val preparedAt: Instant?,
    val backdatedOccurredAt: Boolean,
    val copaymentPaid: Boolean? = false,
    val diagnosisIds: JsonArray,
    val encounterItems: List<EncounterItemApi>,
    val referrals: List<ReferralApi>,
    val visitType: String?,
    val revisedEncounterId: UUID?,
    val providerComment: String?,
    val claimId: String,
    val submittedAt: Instant?,
    val patientOutcome: String?,
    val visitReason: String?,
    val inboundReferralDate: LocalDate?
) {
    constructor(encounter: Encounter, referrals: List<Referral>, encounterItems: List<EncounterItem>): this(
        id = encounter.id,
        memberId = encounter.memberId,
        identificationEventId = encounter.identificationEventId,
        occurredAt = encounter.occurredAt,
        preparedAt = encounter.preparedAt,
        backdatedOccurredAt = encounter.backdatedOccurredAt,
        copaymentPaid = encounter.copaymentPaid,
        diagnosisIds = Gson().fromJson(encounter.diagnoses.toString(), JsonArray::class.java),
        encounterItems = encounterItems.map { EncounterItemApi(it) },
        referrals = referrals.map { ReferralApi(it) },
        visitType = encounter.visitType,
        revisedEncounterId = encounter.revisedEncounterId,
        providerComment = encounter.providerComment,
        claimId = encounter.claimId,
        submittedAt = encounter.submittedAt,
        patientOutcome = encounter.patientOutcome?.toString()?.toLowerCase(),
        visitReason = encounter.visitReason?.toString()?.toLowerCase(),
        inboundReferralDate = encounter.inboundReferralDate
    )

    constructor(encounterWithExtras: EncounterWithExtras): this(
        encounter = encounterWithExtras.encounter,
        referrals = listOfNotNull(encounterWithExtras.referral),
        encounterItems = encounterWithExtras.encounterItemRelations.map { it.encounterItem }
    )
}
