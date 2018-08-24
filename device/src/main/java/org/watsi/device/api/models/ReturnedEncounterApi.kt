package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithExtras
import java.util.UUID

/**
 * Data class that defines the structure of a fetch returned Encounter API request.
 *
 * Uses Strings for Date/Time fields because GSON does not natively support serializing java.time
 * classes to a format our API accepts.
 */
data class ReturnedEncounterApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("member_id") val memberId: UUID,
        @SerializedName("occurred_at") val occurredAt: Instant,
        @SerializedName("backdated_occurred_at") val backdatedOccurredAt: Boolean,
        @SerializedName("diagnosis_ids") val diagnoses: List<Int>,
        @SerializedName("visit_type") val visitType: String?,
        @SerializedName("claim_id") val claimId: String,
        @SerializedName("adjudication_state") val adjudicationState: String,
        @SerializedName("adjudicated_at") val adjudicatedAt: Instant,
        @SerializedName("return_reason") val returnReason: String,
        @SerializedName("revised_encounter_id") val revisedEncounterId: UUID,
        @SerializedName("provider_comment") val providerComment: String,
        // Below are inflated fields.
        @SerializedName("member") val memberApi: MemberApi,
        @SerializedName("billables") val billablesApi: List<BillableApi>,
        @SerializedName("encounter_items") val encounterItemsApi: List<EncounterItemApi>
) {

    fun toEncounterWithExtras(): EncounterWithExtras {
        return EncounterWithExtras(
            encounter = Encounter(
                id = id,
                memberId = memberId,
                identificationEventId = null,
                copaymentPaid = null,
                occurredAt = occurredAt,
                backdatedOccurredAt = backdatedOccurredAt,
                diagnoses = diagnoses,
                visitType = visitType,
                claimId = claimId,
                adjudicationState = Encounter.AdjudicationState.valueOf(adjudicationState.toUpperCase()),
                adjudicatedAt = adjudicatedAt,
                returnReason = returnReason,
                revisedEncounterId = revisedEncounterId,
                providerComment = providerComment
            ),
            encounterItems = combineEncounterItemsWithBillables(
                encounterItemsApi.map { it.toEncounterItem() },
                billablesApi.map { it.toBillable() }
            ),
            member = memberApi.toMember(null),
            encounterForms = emptyList(),
            diagnoses = emptyList() // We don't actually use this field for fetching / persisting.
        )
    }

    private fun combineEncounterItemsWithBillables(
        encounterItems: List<EncounterItem>,
        billables: List<Billable>
    ): List<EncounterItemWithBillable> {
        return encounterItems.map { encounterItem ->
            val correspondingBillable = billables.find { it.id == encounterItem.billableId }
            if (correspondingBillable != null) {
                EncounterItemWithBillable(encounterItem, correspondingBillable)
            } else {
                throw IllegalStateException("Backend returned a encounterItem with a billable thats not inflated in the Encounter. EncounterItem: $encounterItem \n Billables: $billables \n EncounterItems: $encounterItem")
            }
        }
    }
}
