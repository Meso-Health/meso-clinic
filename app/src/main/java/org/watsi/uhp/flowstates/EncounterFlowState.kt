package org.watsi.uhp.flowstates

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Referral
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras
import java.io.Serializable

data class EncounterFlowState(
    var encounter: Encounter,
    var encounterItemRelations: List<EncounterItemWithBillableAndPrice>,
    var encounterForms: List<EncounterForm>,
    var referral: Referral?,
    var member: Member,
    var diagnoses: List<Diagnosis>,
    var newProviderComment: String? = null,
    var originalEncounterWithExtras: EncounterWithExtras? = null
) : Serializable {

    companion object {
        fun fromEncounterWithExtras(encounterWithExtras: EncounterWithExtras): EncounterFlowState {
            return EncounterFlowState(
                encounter = encounterWithExtras.encounter,
                encounterItemRelations = encounterWithExtras.encounterItemRelations,
                encounterForms = encounterWithExtras.encounterForms,
                diagnoses = encounterWithExtras.diagnoses,
                member = encounterWithExtras.member,
                referral = encounterWithExtras.referral,
                originalEncounterWithExtras = encounterWithExtras
            )
        }
    }

    private fun clearEncounterFormThumbnails(): List<EncounterForm> {
        return encounterForms.map { encounterForm ->
            EncounterForm(encounterForm.id, encounterForm.encounterId, encounterForm.photoId, null)
        }
    }

    fun price(): Int = EncounterWithExtras.price(encounterItemRelations)

    fun toEncounterWithExtras(): EncounterWithExtras {
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

    fun hasChanges(): Boolean {
        return originalEncounterWithExtras != this.toEncounterWithExtras() || newProviderComment != null
    }
}
