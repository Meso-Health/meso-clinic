package org.watsi.device.api.models

import com.google.gson.JsonObject
import org.threeten.bp.Instant
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

data class IdentificationEventApi(
    val id: UUID,
    val memberId: UUID,
    val throughMemberId: UUID?,
    val occurredAt: Instant,
    val searchMethod: String,
    val clinicNumber: Int?,
    val clinicNumberType: String?,
    val fingerprintsVerificationResultCode: Int?,
    val fingerprintsVerificationConfidence: Float?,
    val fingerprintsVerificationTier: String?
) {

    constructor (idEvent: IdentificationEvent) :
        this(id = idEvent.id,
             memberId = idEvent.memberId,
             throughMemberId = idEvent.throughMemberId,
             occurredAt = idEvent.occurredAt,
             searchMethod = idEvent.searchMethod.toString().toLowerCase(),
             clinicNumber = idEvent.clinicNumber,
             clinicNumberType = if (idEvent.clinicNumberType == null) null
                     else idEvent.clinicNumberType.toString().toLowerCase(),
             fingerprintsVerificationResultCode = idEvent.fingerprintsVerificationResultCode,
             fingerprintsVerificationConfidence = idEvent.fingerprintsVerificationConfidence,
             fingerprintsVerificationTier = idEvent.fingerprintsVerificationTier
        )

    companion object {
        const val ID_FIELD = "id"
        const val DISMISSED_FIELD = "dismissed"

        fun patch(identificationEvent: IdentificationEvent, deltas: List<Delta>): JsonObject {
            val patchParams = JsonObject()
            patchParams.addProperty(ID_FIELD, identificationEvent.id.toString())
            deltas.forEach { delta ->
                when (delta.field) {
                    DISMISSED_FIELD -> patchParams.addProperty(DISMISSED_FIELD, identificationEvent.dismissed)
                    null -> Unit
                }
            }
            return patchParams
        }
    }
}
