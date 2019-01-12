package org.watsi.device.api.models

import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant
import org.watsi.domain.relations.EncounterWithItems
import java.util.UUID

data class EncounterApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("member_id") val memberId: UUID,
        @SerializedName("identification_event_id") val identificationEventId: UUID?,
        @SerializedName("occurred_at") val occurredAt: Instant,
        @SerializedName("prepared_at") val preparedAt: Instant?,
        @SerializedName("backdated_occurred_at") val backdatedOccurredAt: Boolean,
        @SerializedName("copayment_paid") val copaymentPaid: Boolean? = false,
        @SerializedName("diagnosis_ids") val diagnoses: JsonArray,
        @SerializedName("encounter_items") val encounterItems: List<EncounterItemApi>,
        @SerializedName("visit_type") val visitType: String?,
        @SerializedName("revised_encounter_id") val revisedEncounterId: UUID?,
        @SerializedName("provider_comment") val providerComment: String?,
        @SerializedName("claim_id") val claimId: String,
        @SerializedName("submitted_at") val submittedAt: Instant?
) {

    constructor (encounterWithItems: EncounterWithItems) : this(
        id = encounterWithItems.encounter.id,
        memberId = encounterWithItems.encounter.memberId,
        identificationEventId = encounterWithItems.encounter.identificationEventId,
        occurredAt = encounterWithItems.encounter.occurredAt,
        preparedAt = encounterWithItems.encounter.preparedAt,
        backdatedOccurredAt = encounterWithItems.encounter.backdatedOccurredAt,
        copaymentPaid  = encounterWithItems.encounter.copaymentPaid,
        diagnoses = Gson().fromJson(encounterWithItems.encounter.diagnoses.toString(), JsonArray::class.java),
        encounterItems = encounterWithItems.encounterItems.map { EncounterItemApi(it) },
        visitType = encounterWithItems.encounter.visitType,
        revisedEncounterId = encounterWithItems.encounter.revisedEncounterId,
        providerComment = encounterWithItems.encounter.providerComment,
        claimId = encounterWithItems.encounter.claimId,
        submittedAt = encounterWithItems.encounter.submittedAt
    )
}
