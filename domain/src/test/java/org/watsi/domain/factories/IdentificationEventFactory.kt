package org.watsi.domain.factories

import org.threeten.bp.Instant
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

object IdentificationEventFactory {

    fun build(id: UUID = UUID.randomUUID(),
              memberId: UUID = UUID.randomUUID(),
              throughMemberId: UUID = UUID.randomUUID(),
              occurredAt: Instant = Instant.now(),
              searchMethod: IdentificationEvent.SearchMethod =
                      IdentificationEvent.SearchMethod.SCAN_BARCODE,
              clinicNumber: Int? = 1,
              clinicNumberType: IdentificationEvent.ClinicNumberType? =
                      IdentificationEvent.ClinicNumberType.OPD,
              dismissed: Boolean = false,
              fingerprintsVerificationResultCode: Int? = null,
              fingerprintsVerificationConfidence: Float? = null,
              fingerprintsVerificationTier: String? = null) : IdentificationEvent {
        return IdentificationEvent(id = id,
                                   memberId = memberId,
                                   throughMemberId = throughMemberId,
                                   occurredAt = occurredAt,
                                   searchMethod = searchMethod,
                                   clinicNumber = clinicNumber,
                                   clinicNumberType = clinicNumberType,
                                   dismissed = dismissed,
                                   fingerprintsVerificationResultCode =
                                        fingerprintsVerificationResultCode,
                                   fingerprintsVerificationConfidence =
                                        fingerprintsVerificationConfidence,
                                   fingerprintsVerificationTier = fingerprintsVerificationTier)
    }
}
