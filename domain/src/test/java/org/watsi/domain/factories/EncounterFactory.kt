package org.watsi.domain.factories

import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import java.util.UUID

object EncounterFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        memberId: UUID = UUID.randomUUID(),
        identificationEventId: UUID? = UUID.randomUUID(),
        occurredAt: Instant = Instant.now(),
        preparedAt: Instant? = Instant.now(),
        backdatedOccurredAt: Boolean = false,
        copaymentPaid: Boolean? = true,
        diagnoses: List<Int> = emptyList(),
        visitType: String? = null,
        claimId: String? = null,
        patientOutcome: Encounter.PatientOutcome? = null,
        adjudicationState: Encounter.AdjudicationState? = null,
        adjudicatedAt: Instant? = null,
        adjudicationReason: String? = null,
        revisedEncounterId: UUID? = null,
        providerComment: String? = null,
        submittedAt: Instant? = null,
        visitReason: Encounter.VisitReason? = null,
        inboundReferralDate: Instant? = null
    ) : Encounter {
        return Encounter(
            id = id,
            memberId = memberId,
            identificationEventId = identificationEventId,
            occurredAt = occurredAt,
            preparedAt = preparedAt,
            backdatedOccurredAt = backdatedOccurredAt,
            copaymentPaid = copaymentPaid,
            diagnoses = diagnoses,
            visitType = visitType,
            claimId = claimId ?: id.toString(),
            patientOutcome = patientOutcome,
            adjudicationState = adjudicationState,
            adjudicatedAt = adjudicatedAt,
            adjudicationReason = adjudicationReason,
            revisedEncounterId = revisedEncounterId,
            providerComment = providerComment,
            submittedAt = submittedAt,
            visitReason = visitReason,
            inboundReferralDate = inboundReferralDate
        )
    }
}
