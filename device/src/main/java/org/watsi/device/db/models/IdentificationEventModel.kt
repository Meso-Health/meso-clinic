package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

@Entity(tableName = "identification_events")
data class IdentificationEventModel(@PrimaryKey val id: UUID,
                                    val createdAt: Instant,
                                    val updatedAt: Instant,
                                    val memberId: UUID,
                                    val throughMemberId: UUID?,
                                    val occurredAt: Instant,
                                    val accepted: Boolean,
                                    val searchMethod: IdentificationEvent.SearchMethod,
                                    val clinicNumber: Int,
                                    val clinicNumberType: IdentificationEvent.ClinicNumberType,
                                    val dismissed: Boolean,
                                    val fingerprintsVerificationResultCode: Int?,
                                    val fingerprintsVerificationConfidence: Float?,
                                    val fingerprintsVerificationTier: String?) {

    fun toIdentificationEvent(): IdentificationEvent {
        return IdentificationEvent(id = id,
                                   memberId = memberId,
                                   throughMemberId = throughMemberId,
                                   occurredAt = occurredAt,
                                   accepted = accepted,
                                   searchMethod = searchMethod,
                                   clinicNumber = clinicNumber,
                                   clinicNumberType = clinicNumberType,
                                   dismissed = dismissed,
                                   fingerprintsVerificationResultCode = fingerprintsVerificationResultCode,
                                   fingerprintsVerificationConfidence = fingerprintsVerificationConfidence,
                                   fingerprintsVerificationTier = fingerprintsVerificationTier)
    }

    companion object {
        fun fromIdentificationEvent(idEvent: IdentificationEvent,
                                    clock: Clock): IdentificationEventModel {
            val now = clock.instant()
            return IdentificationEventModel(id = idEvent.id,
                                            createdAt = now,
                                            updatedAt = now,
                                            memberId = idEvent.memberId,
                                            throughMemberId = idEvent.throughMemberId,
                                            occurredAt = idEvent.occurredAt,
                                            accepted = idEvent.accepted,
                                            searchMethod = idEvent.searchMethod,
                                            clinicNumber = idEvent.clinicNumber,
                                            clinicNumberType = idEvent.clinicNumberType,
                                            dismissed = idEvent.dismissed,
                                            fingerprintsVerificationResultCode =
                                                idEvent.fingerprintsVerificationResultCode,
                                            fingerprintsVerificationConfidence =
                                                idEvent.fingerprintsVerificationConfidence,
                                            fingerprintsVerificationTier =
                                                idEvent.fingerprintsVerificationTier)
        }
    }
}
