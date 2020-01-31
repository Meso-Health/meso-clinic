package org.watsi.domain.entities

import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import java.io.Serializable
import java.util.UUID

data class Encounter(
    val id: UUID,
    val memberId: UUID,
    val identificationEventId: UUID?,
    val occurredAt: Instant,
    val patientOutcome: PatientOutcome?,
    val preparedAt: Instant? = null,
    val backdatedOccurredAt: Boolean = false,
    val copaymentAmount: Int = 0,
    val diagnoses: List<Int> = emptyList(),
    val visitType: String? = null,
    val claimId: String = id.toString(),
    val adjudicationState: Encounter.AdjudicationState? = null,
    val adjudicatedAt: Instant? = null,
    val adjudicationReason: String? = null,
    val revisedEncounterId: UUID? = null,
    val providerComment: String? = null,
    val submittedAt: Instant? = null,
    val visitReason: VisitReason? = null,
    val inboundReferralDate: LocalDate? = null,
    val hasFever: Boolean? = null
) : Serializable {

    fun shortenedClaimId(): String {
        return claimId.toUpperCase().substring(CLAIM_ID_RANGE)
    }

    enum class AdjudicationState { PENDING, RETURNED, REVISED, APPROVED }

    enum class EncounterAction { PREPARE, SUBMIT, RESUBMIT }

    enum class PatientOutcome { CURED_OR_DISCHARGED, REFERRED, FOLLOW_UP, DECEASED, OTHER }

    enum class VisitReason { REFERRAL, NO_REFERRAL, FOLLOW_UP, EMERGENCY }

    companion object {
        val VISIT_TYPE_CHOICES = listOf(
            "Outpatient (OPD)",
            "<5 Outpatient (OPD)",
            "Emergency OPD",
            "Inpatient (IPD)",
            "ART",
            "TB",
            "Family Planning (FP)",
            "Antenatal Care (ANC)  - 1st Visit",
            "Antenatal Care (ANC)  - 2nd Visit",
            "Antenatal Care (ANC)  - 3rd Visit",
            "Antenatal Care (ANC)  - 4th Visit",
            "Delivery (DR)"
        )

        val CLAIM_ID_RANGE = (0..7)
    }
}
