package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras
import java.util.UUID

data class ReturnedEncounterApi(
        @SerializedName("id") val id: UUID,
        @SerializedName("member_id") val memberId: UUID,
        @SerializedName("identification_event_id") val identificationEventId: UUID?,
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
        @SerializedName("prepared_at") val preparedAt: Instant,
        @SerializedName("submitted_at") val submittedAt: Instant,
        // Below are inflated fields.
        @SerializedName("member") val memberApi: MemberApi,
        @SerializedName("billables") val billablesApi: List<BillableApi>,
        @SerializedName("price_schedules") val priceSchedulesApi: List<PriceScheduleApi>,
        @SerializedName("encounter_items") val encounterItemsApi: List<EncounterItemApi>
) {

    fun toEncounterWithExtras(): EncounterWithExtras {
        return EncounterWithExtras(
            encounter = Encounter(
                id = id,
                memberId = memberId,
                identificationEventId = identificationEventId,
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
                providerComment = providerComment,
                preparedAt = preparedAt,
                submittedAt = submittedAt
            ),
            encounterItemRelations = combineEncounterItemsWithBillablesAndPrices(
                encounterItemsApi.map { it.toEncounterItem() },
                billablesApi.map { it.toBillable() },
                priceSchedulesApi.map { it.toPriceSchedule() }
            ),
            member = memberApi.toMember(null),
            encounterForms = emptyList(),
            diagnoses = emptyList() // We don't actually use this field for fetching / persisting.
        )
    }

    private fun combineEncounterItemsWithBillablesAndPrices(
        encounterItems: List<EncounterItem>,
        billables: List<Billable>,
        priceSchedules: List<PriceSchedule>
    ): List<EncounterItemWithBillableAndPrice> {
        return encounterItems.map { encounterItem ->
            val correspondingPriceSchedule = priceSchedules.find { it.id == encounterItem.priceScheduleId }
            if (correspondingPriceSchedule != null) {
                val previousPriceSchedule = priceSchedules.find { it.id == correspondingPriceSchedule.previousPriceScheduleModelId }
                val correspondingBillable = billables.find { it.id == correspondingPriceSchedule.billableId }

                if (correspondingBillable != null) {
                    val billableWithPriceSchedule = BillableWithPriceSchedule(correspondingBillable, correspondingPriceSchedule, previousPriceSchedule)
                    EncounterItemWithBillableAndPrice(encounterItem, billableWithPriceSchedule)
                } else {
                    throw IllegalStateException("Backend returned a PriceSchedule with a Billable that's not inflated in the Encounter. " +
                            "EncounterItem: $encounterItem \n PriceSchedule: $correspondingPriceSchedule \n Billables: $billables \n")
                }
            } else {
                throw IllegalStateException("Backend returned an EncounterItem with a PriceSchedule that's not inflated in the Encounter. " +
                        "EncounterItem: $encounterItem \n PriceSchedules: $priceSchedules")
            }
        }
    }
}
