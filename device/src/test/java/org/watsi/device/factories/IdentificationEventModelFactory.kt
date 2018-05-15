package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.IdentificationEventDao
import org.watsi.device.db.models.IdentificationEventModel
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

object IdentificationEventModelFactory {

    fun build(id: UUID = UUID.randomUUID(),
              memberId: UUID = UUID.randomUUID(),
              throughMemberId: UUID = UUID.randomUUID(),
              occurredAt: Instant = Instant.now(),
              searchMethod: IdentificationEvent.SearchMethod =
                      IdentificationEvent.SearchMethod.SCAN_BARCODE,
              clinicNumber: Int = 1,
              clinicNumberType: IdentificationEvent.ClinicNumberType =
                      IdentificationEvent.ClinicNumberType.OPD,
              dismissed: Boolean = false,
              fingerprintsVerificationResultCode: Int? = null,
              fingerprintsVerificationConfidence: Float? = null,
              fingerprintsVerificationTier: String? = null,
              createdAt: Instant? = null,
              updatedAt: Instant? = null,
              clock: Clock = Clock.systemUTC()) : IdentificationEventModel {
        val now = Instant.now(clock)
        return IdentificationEventModel(id = id,
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
                                        fingerprintsVerificationTier = fingerprintsVerificationTier,
                                        createdAt = createdAt ?: now,
                                        updatedAt = updatedAt ?: now)
    }

    fun create(identificationEventDao: IdentificationEventDao,
               id: UUID = UUID.randomUUID(),
               memberId: UUID = UUID.randomUUID(),
               throughMemberId: UUID = UUID.randomUUID(),
               occurredAt: Instant = Instant.now(),
               searchMethod: IdentificationEvent.SearchMethod =
                       IdentificationEvent.SearchMethod.SCAN_BARCODE,
               clinicNumber: Int = 1,
               clinicNumberType: IdentificationEvent.ClinicNumberType =
                       IdentificationEvent.ClinicNumberType.OPD,
               dismissed: Boolean = false,
               fingerprintsVerificationResultCode: Int? = null,
               fingerprintsVerificationConfidence: Float? = null,
               fingerprintsVerificationTier: String? = null,
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               clock: Clock = Clock.systemUTC()) : IdentificationEventModel {
        val model = build(id = id,
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
                          fingerprintsVerificationTier = fingerprintsVerificationTier,
                          createdAt = createdAt,
                          updatedAt = updatedAt,
                          clock = clock)
        identificationEventDao.insert(model)
        return model
    }
}
