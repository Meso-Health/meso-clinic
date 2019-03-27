package org.watsi.domain.entities

import org.threeten.bp.LocalDate
import java.io.Serializable
import java.util.UUID

data class Referral(
    val id: UUID,
    val receivingFacility: String,
    val reason: Reason,
    val encounterId: UUID,
    val number: String?,
    val date: LocalDate
): Serializable {

    enum class Reason {
        FURTHER_CONSULTATION,
        DRUG_STOCKOUT,
        INVESTIGATIVE_TESTS,
        INPATIENT_CARE,
        BED_SHORTAGE,
        FOLLOW_UP,
        OTHER
    }
}
