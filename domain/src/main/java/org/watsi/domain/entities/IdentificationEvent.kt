package org.watsi.domain.entities

import org.threeten.bp.Instant
import java.io.Serializable
import java.util.UUID

data class IdentificationEvent(val id: UUID,
                               val memberId: UUID,
                               val throughMemberId: UUID?,
                               val occurredAt: Instant,
                               val searchMethod: SearchMethod,
                               val clinicNumber: Int?,
                               val clinicNumberType: ClinicNumberType?,
                               val dismissed: Boolean = false) : Serializable {

    enum class ClinicNumberType { OPD, DELIVERY }

    enum class SearchMethod {
        SCAN_BARCODE,
        SEARCH_CARD_ID,
        SEARCH_NAME,
        SEARCH_MEMBERSHIP_NUMBER,
        MANUAL_ENTRY,
        THROUGH_HOUSEHOLD,
        UNKNOWN
    }

    fun formatClinicNumber(): String {
        return when (clinicNumberType) {
            ClinicNumberType.OPD -> {
                "#$clinicNumber"
            }
            ClinicNumberType.DELIVERY -> {
                "#D$clinicNumber"
            }
            else -> {
                ""
            }
        }
    }
}
