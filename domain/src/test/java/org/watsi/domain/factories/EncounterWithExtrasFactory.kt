package org.watsi.domain.factories

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras

object EncounterWithExtrasFactory {
    fun build(
        encounter: Encounter = EncounterFactory.build(),
        encounterItemRelations: List<EncounterItemWithBillableAndPrice> = listOf(
            EncounterItemWithBillableAndPriceFactory.build(), EncounterItemWithBillableAndPriceFactory.build()),
        member: Member = MemberFactory.build(),
        diagnoses: List<Diagnosis> = emptyList(),
        encounterForms: List<EncounterForm> = emptyList()
    ) : EncounterWithExtras {
        return EncounterWithExtras(encounter, member, encounterItemRelations, diagnoses, encounterForms)
    }
}
