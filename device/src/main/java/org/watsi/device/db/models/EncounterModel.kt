package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import java.util.UUID

@Entity(
    tableName = "encounters",
    indices = [
        Index("memberId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = MemberModel::class,
            parentColumns = ["id"],
            childColumns = ["memberId"]
        )
        // We don't have a foreign key for revisedEncounterId & identificationEventId because when fetching returned encounters,
        // we won't necessarily have access to the previous encounter.
    ]
)
data class EncounterModel(
    @PrimaryKey val id: UUID,
    val createdAt: Instant,
    val updatedAt: Instant,
    val memberId: UUID,
    val identificationEventId: UUID?,
    val occurredAt: Instant,
    val preparedAt: Instant?,
    val backdatedOccurredAt: Boolean,
    val copaymentPaid: Boolean?,
    val diagnoses: List<Int>,
    val visitType: String?,
    val claimId: String,
    val patientOutcome: Encounter.PatientOutcome?,
    val adjudicationState: Encounter.AdjudicationState? = null,
    val adjudicatedAt: Instant? = null,
    val adjudicationReason: String? = null,
    val revisedEncounterId: UUID? = null,
    val providerComment: String? = null,
    val submittedAt: Instant? = null,
    val visitReason: Encounter.VisitReason? = null,
    val inboundReferralDate: Instant? = null
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
            patientOutcome = patientOutcome,
            adjudicationState = adjudicationState,
            adjudicatedAt = adjudicatedAt,
            adjudicationReason = adjudicationReason,
            revisedEncounterId = revisedEncounterId,
            providerComment = providerComment,
            preparedAt = preparedAt,
            submittedAt = submittedAt,
            visitReason = visitReason,
            inboundReferralDate = inboundReferralDate
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
                patientOutcome = encounter.patientOutcome,
                adjudicationState = encounter.adjudicationState,
                adjudicatedAt = encounter.adjudicatedAt,
                adjudicationReason = encounter.adjudicationReason,
                revisedEncounterId = encounter.revisedEncounterId,
                providerComment = encounter.providerComment,
                preparedAt = encounter.preparedAt ?: now,
                submittedAt = encounter.submittedAt,
                visitReason = encounter.visitReason,
                inboundReferralDate = encounter.inboundReferralDate
            )
        }
    }
}
