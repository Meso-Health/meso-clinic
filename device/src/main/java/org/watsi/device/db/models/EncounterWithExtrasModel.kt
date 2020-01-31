package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.threeten.bp.Clock
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.relations.EncounterWithExtras

data class EncounterWithExtrasModel(
    @Embedded var encounterModel: EncounterModel? = null,
    @Relation(parentColumn = "memberId", entityColumn = "id", entity = MemberModel::class)
    var memberModel: List<MemberModel>? = null,
    @Relation(parentColumn = "id", entityColumn = "encounterId", entity = EncounterItemModel::class)
    var encounterItemWithBillableAndPriceModels: List<EncounterItemWithBillableAndPriceModel>? = null,
    @Relation(parentColumn = "id", entityColumn = "encounterId", entity = EncounterFormModel::class)
    var encounterFormModels: List<EncounterFormModel>? = null,
    @Relation(parentColumn = "id", entityColumn = "encounterId", entity = ReferralModel::class)
    var referralModels: List<ReferralModel>? = null
) {

    // The reason we need to pass in a list of diagnosis is the modelling is not quite right for how
    // we store diagnoses. There's no way in android room to add a @Relation above for diagnoses
    // because we stored the diagnosis field on Encounter as a string list array, instead of the
    // encounter_id on the a separate table (i.e. DiagnosisItem) table.
    fun toEncounterWithExtras(diagnoses: List<Diagnosis>): EncounterWithExtras {
        encounterModel?.toEncounter()?.let { encounter ->
            memberModel?.firstOrNull()?.toMember()?.let { member ->
                val items =
                    encounterItemWithBillableAndPriceModels?.map { it.toEncounterItemWithBillableAndPrice() }
                        ?: emptyList()
                val forms = encounterFormModels?.map { it.toEncounterForm() } ?: emptyList()
                val referrals = referralModels?.map { it.toReferral() } ?: emptyList()

                return EncounterWithExtras(
                    encounter = encounter,
                    member = member,
                    encounterItemRelations = items,
                    referral = referrals.firstOrNull(),
                    encounterForms = forms,
                    diagnoses = diagnoses
                )
            }
            throw IllegalStateException("Member cannot be null")
        }
        throw IllegalStateException("EncounterModel cannot be null")
    }

    companion object {
        fun fromEncounterWithExtras(encounterWithExtras: EncounterWithExtras, clock: Clock): EncounterWithExtrasModel {
            val referral = encounterWithExtras.referral
            val referralModels: List<ReferralModel>
            referralModels = if (referral != null) {
                listOf(ReferralModel.fromReferral(referral))
            } else {
                emptyList()
            }
            return EncounterWithExtrasModel(
                encounterModel = EncounterModel.fromEncounter(encounterWithExtras.encounter, clock),
                memberModel = listOf(MemberModel.fromMember(encounterWithExtras.member, clock)),
                encounterItemWithBillableAndPriceModels = encounterWithExtras.encounterItemRelations.map {
                    EncounterItemWithBillableAndPriceModel(
                        encounterItemModel = EncounterItemModel.fromEncounterItem(it.encounterItem, clock),
                        priceScheduleWithBillableModel = listOf(PriceScheduleWithBillableModel(
                            priceScheduleModel = PriceScheduleModel.fromPriceSchedule(it.billableWithPriceSchedule.priceSchedule, clock),
                            billableModel = listOf(BillableModel.fromBillable(it.billableWithPriceSchedule.billable, clock))
                        ))
                    )
                },
                encounterFormModels = emptyList(),
                referralModels = referralModels
            )
        }
    }
}
