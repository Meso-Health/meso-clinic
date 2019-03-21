package org.watsi.domain.factories

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Referral
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithExtras

object EncounterWithExtrasFactory {
    fun build(
        member: Member = MemberFactory.build(),
        encounter: Encounter = EncounterFactory.build(memberId = member.id),
        encounterItemRelations: List<EncounterItemWithBillableAndPrice> = listOf(
            EncounterItemWithBillableAndPriceFactory.buildWithEncounter(encounterId = encounter.id),
            EncounterItemWithBillableAndPriceFactory.buildWithEncounter(encounterId = encounter.id)
        ),
        diagnoses: List<Diagnosis> = emptyList(),
        encounterForms: List<EncounterForm> = emptyList(),
        referral: Referral? = null
    ) : EncounterWithExtras {
        return EncounterWithExtras(
            encounter = encounter,
            member = member,
            encounterItemRelations = encounterItemRelations,
            diagnoses = diagnoses,
            encounterForms = encounterForms,
            referral = referral
        )
    }
}
