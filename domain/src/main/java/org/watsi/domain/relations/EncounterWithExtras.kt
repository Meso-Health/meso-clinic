package org.watsi.domain.relations

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Referral
import java.io.Serializable

data class EncounterWithExtras(
    val encounter: Encounter,
    val member: Member,
    val encounterItemRelations: List<EncounterItemWithBillableAndPrice>,
    val diagnoses: List<Diagnosis>,
    val encounterForms: List<EncounterForm>,
    val referral: Referral?
) : Serializable {
    fun toEncounterWithItemsAndForms(): EncounterWithItemsAndForms {
        return EncounterWithItemsAndForms(
            encounter = encounter,
            encounterItemRelations = encounterItemRelations,
            encounterForms = encounterForms,
            referral = referral
        )
    }

    fun price(): Int = encounterItemRelations.sumBy { it.price() }
}
