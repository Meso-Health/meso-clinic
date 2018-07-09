package org.watsi.device.api.models

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant
import org.watsi.domain.relations.EncounterWithItems
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
        @SerializedName("identification_event_id") val identificationEventId: UUID?,
        @SerializedName("occurred_at") val occurredAt: Instant,
        @SerializedName("backdated_occurred_at") val backdatedOccurredAt: Boolean,
        @SerializedName("copayment_paid") val copaymentPaid: Boolean = false,
        @SerializedName("diagnosis_ids") val diagnoses: JsonArray,
        @SerializedName("encounter_items") val encounterItems: List<EncounterItemApi>
) {

    constructor (encounterWithItems: EncounterWithItems) :
            this(id = encounterWithItems.encounter.id,
                 memberId = encounterWithItems.encounter.memberId,
                 identificationEventId = encounterWithItems.encounter.identificationEventId,
                 occurredAt = encounterWithItems.encounter.occurredAt,
                 backdatedOccurredAt = encounterWithItems.encounter.backdatedOccurredAt,
                 copaymentPaid  = encounterWithItems.encounter.copaymentPaid,
                 diagnoses = Gson().fromJson(encounterWithItems.encounter.diagnoses.toString(), JsonArray::class.java),
                 encounterItems = encounterWithItems.encounterItems.map { EncounterItemApi(it) }
            )
}
