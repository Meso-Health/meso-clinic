package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import java.util.UUID

@Entity(tableName = "encounters")
data class EncounterModel(
    @PrimaryKey val id: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
    val memberId: UUID,
    val identificationEventId: UUID?,
    val occurredAt: Instant,
    val backdatedOccurredAt: Boolean,
    val copaymentPaid: Boolean?,
    val diagnoses: List<Int>,
    val visitType: String?,
    val claimId: String,
    val adjudicationState: Encounter.AdjudicationState = Encounter.AdjudicationState.PENDING,
    val adjudicatedAt: Instant? = null,
    val returnReason: String? = null,
    val revisedEncounterId: UUID? = null,
    val providerComment: String? = null,
    val preparedAt: Instant,
    val submittedAt: Instant? = null
) {

    fun toEncounter(): Encounter {
        return Encounter(
            id = id,
            memberId = memberId,
            identificationEventId = identificationEventId,
            occurredAt = occurredAt,
            backdatedOccurredAt = backdatedOccurredAt,
            copaymentPaid = copaymentPaid,
            diagnoses = diagnoses,
            visitType = visitType,
            claimId = claimId,
            adjudicationState = adjudicationState,
            adjudicatedAt = adjudicatedAt,
            returnReason = returnReason,
            revisedEncounterId = revisedEncounterId,
            providerComment = providerComment,
            preparedAt = preparedAt,
            submittedAt = submittedAt
        )
    }

    companion object {
        fun fromEncounter(encounter: Encounter, clock: Clock): EncounterModel {
            val now = clock.instant()
            return EncounterModel(
                id = encounter.id,
                createdAt = now,
                updatedAt = now,
                memberId = encounter.memberId,
                identificationEventId = encounter.identificationEventId,
                occurredAt = encounter.occurredAt,
                backdatedOccurredAt = encounter.backdatedOccurredAt,
                copaymentPaid = encounter.copaymentPaid,
                diagnoses = encounter.diagnoses,
                visitType = encounter.visitType,
                claimId = encounter.claimId,
                adjudicationState = encounter.adjudicationState,
                adjudicatedAt = encounter.adjudicatedAt,
                returnReason = encounter.returnReason,
                revisedEncounterId = encounter.revisedEncounterId,
                providerComment = encounter.providerComment,
                preparedAt = encounter.preparedAt,
                submittedAt = encounter.submittedAt
            )
        }
    }
}
