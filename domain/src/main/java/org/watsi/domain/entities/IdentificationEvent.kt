package org.watsi.domain.entities

import org.threeten.bp.Instant
import java.io.Serializable
import java.util.UUID

data class IdentificationEvent(val id: UUID,
                               val memberId: UUID,
                               val throughMemberId: UUID?,
                               val occurredAt: Instant,
                               val accepted: Boolean,
                               val searchMethod: SearchMethod,
                               val clinicNumber: Int,
                               val clinicNumberType: ClinicNumberType,
                               val dismissed: Boolean = false,
                               val dismissalReason: DismissalReason? = null,
                               val fingerprintsVerificationResultCode: Int?,
                               val fingerprintsVerificationConfidence: Float?,
                               val fingerprintsVerificationTier: String?) : Serializable {

    enum class ClinicNumberType { OPD, DELIVERY }

    enum class SearchMethod { SCAN_BARCODE, SEARCH_ID, SEARCH_NAME, THROUGH_HOUSEHOLD }

    enum class DismissalReason {
        MEMBER_ON_OTHER_PHONE,
        ACCIDENTAL_IDENTIFICATION,
        MEMBER_LEFT_BEFORE_CARE,
        MEMBER_LEFT_AFTER_CARE
    }
}
