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
        STOCKOUT,
        INVESTIGATIVE_TESTS,
        INPATIENT_CARE,
        FOLLOW_UP,
        ADDITIONAL_SERVICES,
        INSUFFICIENT_EQUIPMENT,
        SURGERY,
        OTHER
    }

    companion object {
        val UNSPECIFIED_FACILTY = "UNSPECIFIED"
        val RECEIVING_FACILITY_CHOICES = listOf(
            "Fort Portal Hospital",
            "Kyenjojo Health Center",
            "Rwibaale Health Center"
        ).sorted()
    }
}
