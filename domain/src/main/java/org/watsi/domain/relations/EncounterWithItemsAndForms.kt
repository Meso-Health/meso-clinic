package org.watsi.domain.relations

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import java.io.Serializable

data class EncounterWithItemsAndForms(val encounter: Encounter,
                                      val encounterItems: List<EncounterItemWithBillable>,
                                      val encounterForms: List<EncounterForm>,
                                      val diagnoses: List<Diagnosis>) : Serializable

data class MutableEncounterWithItemsAndForms(var encounter: Encounter,
                                             var encounterItems: List<EncounterItemWithBillable>,
                                             var encounterForms: List<EncounterForm>,
                                             var diagnoses: List<Diagnosis>) : Serializable {

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
