package org.watsi.device.api.models

import com.google.gson.JsonObject
import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.Delta
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

/**
 * Data class that defines the structure of a sync Identification Event API request.
 *
 * Uses Strings for Date/Time fields because GSON does not natively support serializing java.time
 * classes to a format our API accepts.
 */
data class IdentificationEventApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("member_id") val memberId: UUID,
        @SerializedName("through_member_id") val throughMemberId: UUID?,
        @SerializedName("occurred_at") val occurredAt: String,
        @SerializedName("search_method") val searchMethod: IdentificationEvent.SearchMethod,
        @SerializedName("clinic_number") val clinicNumber: Int,
        @SerializedName("clinic_number_type") val clinicNumberType: IdentificationEvent.ClinicNumberType,
        @SerializedName("dismissed") val dismissed: Boolean = false,
        @SerializedName("fingerprints_verification_result_code") val fingerprintsVerificationResultCode: Int?,
        @SerializedName("fingerprints_verification_confidence") val fingerprintsVerificationConfidence: Float?,
        @SerializedName("fingerprints_verification_tier") val fingerprintsVerificationTier: String?
) {

    constructor (idEvent: IdentificationEvent) :
            this(id = idEvent.id,
                 memberId = idEvent.memberId,
                 throughMemberId = idEvent.throughMemberId,
                 occurredAt = idEvent.occurredAt.toString(),
                 searchMethod = idEvent.searchMethod,
                 clinicNumber = idEvent.clinicNumber,
                 clinicNumberType = idEvent.clinicNumberType,
                 dismissed = idEvent.dismissed,
                 fingerprintsVerificationResultCode = idEvent.fingerprintsVerificationResultCode,
                 fingerprintsVerificationConfidence = idEvent.fingerprintsVerificationConfidence,
                 fingerprintsVerificationTier = idEvent.fingerprintsVerificationTier
            )

    companion object {
        fun patch(identificationEvent: IdentificationEvent, deltas: List<Delta>): JsonObject {
            val patchParams = JsonObject()
            deltas.forEach { delta ->
                when (delta.field) {
                    "dismissed" -> patchParams.addProperty("dismissed", identificationEvent.dismissed)
                    null -> Unit
                }
            }
            return patchParams
        }
    }
}
