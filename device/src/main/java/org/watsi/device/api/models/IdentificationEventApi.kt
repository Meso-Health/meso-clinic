package org.watsi.device.api.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

data class IdentificationEventApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("member_id") val memberId: UUID,
        @SerializedName("through_member_id") val throughMemberId: UUID?,
        @SerializedName("occurred_at") val occurredAt: Instant,
        @SerializedName("search_method") val searchMethod: String,
        @SerializedName("clinic_number") val clinicNumber: Int?,
        @SerializedName("clinic_number_type") val clinicNumberType: String?,
        @SerializedName("fingerprints_verification_result_code") val fingerprintsVerificationResultCode: Int?,
        @SerializedName("fingerprints_verification_confidence") val fingerprintsVerificationConfidence: Float?,
        @SerializedName("fingerprints_verification_tier") val fingerprintsVerificationTier: String?
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
