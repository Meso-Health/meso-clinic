package org.watsi.domain.relations

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import java.io.Serializable

data class EncounterWithExtras(
        val encounter: Encounter,
        val member: Member,
        val encounterItems: List<EncounterItemWithBillable>,
        val diagnoses: List<Diagnosis>,
        val encounterForms: List<EncounterForm>
) : Serializable {
    fun toEncounterWithItemsAndForms(): EncounterWithItemsAndForms {
        return EncounterWithItemsAndForms(
            encounter = encounter,
            encounterItems = encounterItems,
            diagnoses = diagnoses,
            encounterForms = encounterForms
        )
    }

    fun price(): Int = encounterItems.sumBy { it.price() }
}
