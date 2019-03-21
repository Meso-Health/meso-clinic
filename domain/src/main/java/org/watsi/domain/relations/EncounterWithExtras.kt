package org.watsi.domain.relations

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Referral
import java.io.Serializable

data class EncounterWithExtras(
    val encounter: Encounter,
    val encounterItemRelations: List<EncounterItemWithBillableAndPrice>,
    val encounterForms: List<EncounterForm>,
    val referral: Referral?,
    val member: Member,
    val diagnoses: List<Diagnosis>
) : Serializable {
    fun price(): Int = encounterItemRelations.sumBy { it.price() }
}
