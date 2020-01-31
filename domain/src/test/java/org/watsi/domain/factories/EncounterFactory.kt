package org.watsi.domain.factories

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
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
        copaymentAmount: Int = 0,
        diagnoses: List<Int> = emptyList(),
        visitType: String? = null,
        claimId: String? = null,
        patientOutcome: Encounter.PatientOutcome? = null,
        adjudicationState: Encounter.AdjudicationState? = Encounter.AdjudicationState.PENDING,
        adjudicatedAt: Instant? = null,
        adjudicationReason: String? = null,
        revisedEncounterId: UUID? = null,
        providerComment: String? = null,
        submittedAt: Instant? = null,
        visitReason: Encounter.VisitReason? = null,
        inboundReferralDate: LocalDate? = null
    ) : Encounter {
        return Encounter(
            id = id,
            memberId = memberId,
            identificationEventId = identificationEventId,
            occurredAt = occurredAt,
            preparedAt = preparedAt,
            backdatedOccurredAt = backdatedOccurredAt,
            copaymentAmount = copaymentAmount,
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
