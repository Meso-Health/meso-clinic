package org.watsi.uhp.flowstates

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras
import org.watsi.domain.relations.EncounterWithItemsAndForms
import java.io.Serializable

data class EncounterFlowState(var encounter: Encounter,
                              var encounterItemRelations: List<EncounterItemWithBillableAndPrice>,
                              var encounterForms: List<EncounterForm>,
                              var diagnoses: List<Diagnosis>,
                              var member: Member? = null,
                              var newProviderComment: String? = null) : Serializable {

    companion object {
        fun fromEncounterWithExtras(encounterWithExtras: EncounterWithExtras): EncounterFlowState {
            return EncounterFlowState(encounterWithExtras.encounter, encounterWithExtras.encounterItemRelations,
                encounterWithExtras.encounterForms, encounterWithExtras.diagnoses, encounterWithExtras.member
            )
        }
    }

    private fun clearEncounterFormThumbnails(): List<EncounterForm> {
        return encounterForms.map { encounterForm ->
            EncounterForm(encounterForm.id, encounterForm.encounterId, encounterForm.photoId, null)
        }
    }

    fun price(): Int = encounterItemRelations.map { it.price() }.sum()

    fun toEncounterWithItemsAndForms(): EncounterWithItemsAndForms {
        return EncounterWithItemsAndForms(encounter, encounterItemRelations, clearEncounterFormThumbnails(), diagnoses)
    }

    fun toEncounterWithExtras(member: Member): EncounterWithExtras {
        return EncounterWithExtras(encounter, member, encounterItemRelations, diagnoses, encounterForms)
    }

    fun getEncounterItemsOfType(billableType: Billable.Type): List<EncounterItemWithBillableAndPrice> {
        return encounterItemRelations.filter { it.billableWithPriceSchedule.billable.type == billableType }
    }
}
