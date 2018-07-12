package org.watsi.uhp.flowstates

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms
import java.io.Serializable

data class EncounterFlowState(var encounter: Encounter,
                              var encounterItems: List<EncounterItemWithBillable>,
                              var encounterForms: List<EncounterForm>,
                              var diagnoses: List<Diagnosis>,
                              var member: Member? = null) : Serializable {

    private fun clearEncounterFormThumbnails(): List<EncounterForm> {
        return encounterForms.map { encounterForm ->
            EncounterForm(encounterForm.id, encounterForm.encounterId, encounterForm.photoId, null)
        }
    }

    fun price(): Int = encounterItems.map { it.price() }.sum()

    fun toEncounterWithItemsAndForms(): EncounterWithItemsAndForms {
        return EncounterWithItemsAndForms(encounter, encounterItems, clearEncounterFormThumbnails(), diagnoses)
    }
}
