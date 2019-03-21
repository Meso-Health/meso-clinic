package org.watsi.uhp.flowstates

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Referral
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.relations.EncounterWithItemsAndForms
import java.io.Serializable

data class EncounterFlowState(
    var encounter: Encounter,
    var encounterItemRelations: List<EncounterItemWithBillableAndPrice>,
    var encounterForms: List<EncounterForm>,
    var diagnoses: List<Diagnosis>,
    var referral: Referral?,
    var member: Member? = null,
    var newProviderComment: String? = null
) : Serializable {

    companion object {
        fun fromEncounterWithExtras(encounterWithExtras: EncounterWithExtras): EncounterFlowState {
            return EncounterFlowState(
                encounter = encounterWithExtras.encounter,
                encounterItemRelations = encounterWithExtras.encounterItemRelations,
                encounterForms = encounterWithExtras.encounterForms,
                diagnoses = encounterWithExtras.diagnoses,
                member = encounterWithExtras.member,
                referral = encounterWithExtras.referral
            )
        }
    }

    private fun clearEncounterFormThumbnails(): List<EncounterForm> {
        return encounterForms.map { encounterForm ->
            EncounterForm(encounterForm.id, encounterForm.encounterId, encounterForm.photoId, null)
        }
    }

    fun price(): Int = encounterItemRelations.map {
        if (it.encounterItem.stockout) {
            0
        } else {
            it.price()
        }
    }.sum()

    fun toEncounterWithItemsAndForms(): EncounterWithItemsAndForms {
        return EncounterWithItemsAndForms(
            encounter = encounter,
            encounterItemRelations = encounterItemRelations,
            encounterForms = clearEncounterFormThumbnails(),
            referral = referral
        )
    }

    fun toEncounterWithExtras(member: Member): EncounterWithExtras {
        return EncounterWithExtras(
            encounter = encounter,
            member = member,
            encounterItemRelations = encounterItemRelations,
            diagnoses = diagnoses,
            encounterForms = encounterForms,
            referral = referral
        )
    }

    fun getEncounterItemsOfType(billableType: Billable.Type): List<EncounterItemWithBillableAndPrice> {
        return encounterItemRelations.filter { it.billableWithPriceSchedule.billable.type == billableType }
    }
}
