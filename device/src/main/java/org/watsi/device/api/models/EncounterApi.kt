package org.watsi.device.api.models

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import java.util.UUID

/**
 * Data class that defines the structure of a sync Encounter API request.
 *
 * Uses Strings for Date/Time fields because GSON does not natively support serializing java.time
 * classes to a format our API accepts.
 */
data class EncounterApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("member_id") val memberId: UUID,
        @SerializedName("identification_event_id") val identificationEventId: UUID,
        @SerializedName("occurred_at") val occurredAt: Instant,
        @SerializedName("backdated_occurred_at") val backdatedOccurredAt: Instant?,
        @SerializedName("copayment_paid") val copaymentPaid: Boolean = false,
        @SerializedName("diagnosis_ids") val diagnoses: JsonArray,
        @SerializedName("encounter_items") val encounterItems: List<EncounterItemApi>
) {

    constructor (encounter: Encounter, encounterItems: List<EncounterItem>) :
            this(id = encounter.id,
                 memberId = encounter.memberId,
                 identificationEventId = encounter.identificationEventId,
                 occurredAt = encounter.occurredAt,
                 backdatedOccurredAt = encounter.backdatedOccurredAt,
                 copaymentPaid  = encounter.copaymentPaid,
                 diagnoses = Gson().fromJson(encounter.diagnoses.toString(), JsonArray::class.java),
                 encounterItems = encounterItems.map { EncounterItemApi(it) }
            )
}
