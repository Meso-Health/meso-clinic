package org.watsi.domain.factories

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithExtras

object EncounterWithExtrasFactory {
    fun build(member: Member = MemberFactory.build(),
              encounter: Encounter = EncounterFactory.build(memberId = member.id),
              items: List<EncounterItemWithBillable> = emptyList(),
              forms: List<EncounterForm> = emptyList(),
              diagnoses: List<Diagnosis> = emptyList()
    ) : EncounterWithExtras {
        return EncounterWithExtras(encounter, member, items, diagnoses, forms)
    }
}
