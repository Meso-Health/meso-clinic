package org.watsi.device.api.models

import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.PriceSchedule
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras
import java.util.UUID

data class ReturnedEncounterApi(
    val id: UUID,
    val memberId: UUID,
    val identificationEventId: UUID?,
    val occurredAt: Instant,
    val backdatedOccurredAt: Boolean,
    val visitType: String?,
    val claimId: String,
    val patientOutcome: String?,
    val adjudicationState: String,
    val adjudicatedAt: Instant,
    val adjudicationReason: String,
    val revisedEncounterId: UUID,
    val providerComment: String?,
    val preparedAt: Instant,
    val submittedAt: Instant,
    // Below are inflated fields.
    val member: MemberApi,
    val billables: List<BillableApi>,
    val priceSchedules: List<PriceScheduleApi>,
    val encounterItems: List<EncounterItemApi>,
    val diagnosisIds: List<Int>,
    val diagnoses: List<DiagnosisApi>?,
    val referrals: List<ReferralApi>
) {

    fun toEncounterWithExtras(persistedMember: Member? = null): EncounterWithExtras {
        return EncounterWithExtras(
            encounter = Encounter(
                id = id,
                memberId = memberId,
                identificationEventId = identificationEventId,
                copaymentPaid = null,
                occurredAt = occurredAt,
                backdatedOccurredAt = backdatedOccurredAt,
                diagnoses = diagnosisIds,
                visitType = visitType,
                claimId = claimId,
                patientOutcome = patientOutcome?.let { Encounter.PatientOutcome.valueOf(it.toUpperCase()) },
                adjudicationState = Encounter.AdjudicationState.valueOf(adjudicationState.toUpperCase()),
                adjudicatedAt = adjudicatedAt,
                adjudicationReason = adjudicationReason,
                revisedEncounterId = revisedEncounterId,
                providerComment = providerComment,
                preparedAt = preparedAt,
                submittedAt = submittedAt
            ),
            encounterItemRelations = combineEncounterItemsWithBillablesAndPrices(
                encounterItems.map { it.toEncounterItem() },
                billables.map { it.toBillable() },
                priceSchedules.map { it.toPriceSchedule() }
            ),
            member = member.toMember(persistedMember),
            encounterForms = emptyList(),
            diagnoses = diagnoses.orEmpty().map { it.toDiagnosis() },
            // diagnoses defaults to empty list so that we don't need to wait for the corresponding backend changes.
            referral = referrals.firstOrNull()?.toReferral()
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
