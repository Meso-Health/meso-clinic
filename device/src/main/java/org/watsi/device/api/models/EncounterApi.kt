package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.Encounter
import org.watsi.domain.relations.EncounterItemWithBillable
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
        @SerializedName("occurred_at") val occurredAt: String,
        @SerializedName("backdated_occurred_at") val backdatedOccurredAt: String?,
        @SerializedName("copayment_paid") val copaymentPaid: Boolean = false,
        @SerializedName("diagnosis_ids") val diagnoses: String,
        @SerializedName("encounter_items") val encounterItems: List<EncounterItemApi>
) {

    constructor (encounter: Encounter, encounterItems: List<EncounterItemWithBillable>) :
            this(id = encounter.id,
                 memberId = encounter.memberId,
                 identificationEventId = encounter.identificationEventId,
                 occurredAt = encounter.occurredAt.toString(),
                 backdatedOccurredAt = encounter.backdatedOccurredAt.toString(),
                 copaymentPaid  = encounter.copaymentPaid,
                 diagnoses = encounter.diagnoses.toString(),
                 encounterItems = encounterItems.map { EncounterItemApi(it) }
            )
}
