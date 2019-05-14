package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.daos.MemberDao
import org.watsi.device.db.models.EncounterModel
import org.watsi.domain.entities.Encounter
import java.util.UUID

object EncounterModelFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        memberId: UUID,
        identificationEventId: UUID? = null,
        occurredAt: Instant = Instant.now(),
        preparedAt: Instant = Instant.now(),
        backdatedOccurredAt: Boolean = false,
        copaymentPaid: Boolean? = true,
        diagnoses: List<Int> = emptyList(),
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        visitType: String? = null,
        claimId: String? = null,
        patientOutcome: Encounter.PatientOutcome? = null,
        adjudicationState: Encounter.AdjudicationState = Encounter.AdjudicationState.PENDING,
        adjudicatedAt: Instant? = null,
        adjudicationReason: String? = null,
        revisedEncounterId: UUID? = null,
        providerComment: String? = null,
        submittedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ) : EncounterModel {
        val now = Instant.now(clock)
        return EncounterModel(
            id = id,
            memberId = memberId,
            identificationEventId = identificationEventId,
            occurredAt = occurredAt,
            preparedAt = preparedAt,
            backdatedOccurredAt = backdatedOccurredAt,
            copaymentPaid = copaymentPaid,
            diagnoses = diagnoses,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now,
            visitType = visitType,
            patientOutcome = patientOutcome,
            claimId = claimId ?: id.toString(),
            adjudicationState = adjudicationState,
            adjudicatedAt = adjudicatedAt,
            adjudicationReason = adjudicationReason,
            revisedEncounterId = revisedEncounterId,
            providerComment = providerComment,
            submittedAt = submittedAt
        )
    }

    fun create(
        encounterDao: EncounterDao,
        memberDao: MemberDao,
        id: UUID = UUID.randomUUID(),
        memberId: UUID? = null,
        identificationEventId: UUID? = null,
        occurredAt: Instant = Instant.now(),
        preparedAt: Instant = Instant.now(),
        backdatedOccurredAt: Boolean = false,
        copaymentPaid: Boolean? = true,
        diagnoses: List<Int> = emptyList(),
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        visitType: String? = null,
        claimId: String? = null,
        patientOutcome: Encounter.PatientOutcome? = null,
        adjudicationState: Encounter.AdjudicationState = Encounter.AdjudicationState.PENDING,
        adjudicatedAt: Instant? = null,
        adjudicationReason: String? = null,
        revisedEncounterId: UUID? = null,
        providerComment: String? = null,
        submittedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ) : EncounterModel {
        val model = build(
            id = id,
            memberId = memberId ?: MemberModelFactory.create(memberDao).id,
            identificationEventId = identificationEventId,
            occurredAt = occurredAt,
            preparedAt = preparedAt,
            backdatedOccurredAt = backdatedOccurredAt,
            copaymentPaid = copaymentPaid,
            diagnoses = diagnoses,
            createdAt = createdAt,
            updatedAt = updatedAt,
            visitType = visitType,
            claimId = claimId ?: id.toString(),
            patientOutcome = patientOutcome,
            adjudicationState = adjudicationState,
            adjudicatedAt = adjudicatedAt,
            adjudicationReason = adjudicationReason,
            revisedEncounterId = revisedEncounterId,
            providerComment = providerComment,
            submittedAt = submittedAt,
            clock = clock
        )
        encounterDao.insert(model, emptyList(), emptyList(), emptyList(), emptyList())
        return model
    }
}
