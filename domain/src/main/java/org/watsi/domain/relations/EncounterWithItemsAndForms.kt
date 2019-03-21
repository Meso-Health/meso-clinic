package org.watsi.domain.relations

import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Referral
import java.io.Serializable

data class EncounterWithItemsAndForms(
    val encounter: Encounter,
    val encounterItemRelations: List<EncounterItemWithBillableAndPrice>,
    val encounterForms: List<EncounterForm>,
    val referral: Referral?
) : Serializable
