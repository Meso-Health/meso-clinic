package org.watsi.domain.entities

import org.threeten.bp.Instant
import java.io.Serializable
import java.util.UUID

data class Encounter(
    val id: UUID,
    val memberId: UUID,
    val identificationEventId: UUID?,
    val occurredAt: Instant,
    val backdatedOccurredAt: Boolean = false,
    val copaymentPaid: Boolean? = true,
    val diagnoses: List<Int> = emptyList(),
    val visitType: String? = null
) : Serializable {

    companion object {
        val VISIT_TYPE_CHOICES = listOf(
            "OPD - New Visit",
            "OPD - Repeat Visit",
            "ART - New Patient",
            "ART - Repeat Visit",
            "TB - New Patient",
            "TB - Repeat Visit",
            "Youth Friendly Services (YFS) - New Visit",
            "Youth Friendly Services (YFS) - Repeat Visit",
            "Family Planning (FP) - New Visit",
            "Family Planning (FP) - Repeat Visit",
            "Antenatal Care (ANC)  - 1st Visit",
            "Antenatal Care (ANC)  - 2nd Visit",
            "Antenatal Care (ANC)  - 3rd Visit",
            "Antenatal Care (ANC)  - 4th Visit",
            "Postnatal Care (PNC) - 1st Visit",
            "Postnatal Care (PNC) - 2nd Visit",
            "EPI",
            "<5 OPD - New Visit",
            "<5 OPD - Repeat Visit",
            "Inpatient (IPD)",
            "Emergency OPD",
            "Delivery (DR)",
            "Abortion",
            "Growth Monitoring & Promotion (GMP) - New Visit",
            "Growth Monitoring & Promotion (GMP) - Repeat Visit",
            "Dental"
        )
    }
}
