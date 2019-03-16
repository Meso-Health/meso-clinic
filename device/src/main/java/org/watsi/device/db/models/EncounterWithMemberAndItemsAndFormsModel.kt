package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterWithMemberAndItemsAndForms

data class EncounterWithMemberAndItemsAndFormsModel(
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

    fun toEncounterWithMemberAndItemsAndForms(): EncounterWithMemberAndItemsAndForms {
        encounterModel?.toEncounter()?.let { encounter ->
            memberModel?.firstOrNull()?.toMember()?.let { member ->
                val items =
                    encounterItemWithBillableAndPriceModels?.map { it.toEncounterItemWithBillableAndPrice() }
                        ?: emptyList()
                val forms = encounterFormModels?.map { it.toEncounterForm() } ?: emptyList()
                val referrals = referralModels?.map { it.toReferral() } ?: emptyList()

                return EncounterWithMemberAndItemsAndForms(
                    encounter = encounter,
                    member = member,
                    encounterItemRelations = items,
                    referrals = referrals,
                    encounterForms = forms
                )
            }
            throw IllegalStateException("Member cannot be null")
        }
        throw IllegalStateException("EncounterModel cannot be null")
    }
}
