package org.watsi.domain.factories

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithExtras

object EncounterWithExtrasFactory {
    fun build(
            encounter: Encounter = EncounterFactory.build(),
            encounterItems: List<EncounterItemWithBillable> = emptyList(),
            member: Member = MemberFactory.build(),
            diagnoses: List<Diagnosis> = emptyList(),
            encounterForms: List<EncounterForm> = emptyList()
    ) : EncounterWithExtras {
        return EncounterWithExtras(encounter, member, encounterItems, diagnoses, encounterForms)
    }
}
