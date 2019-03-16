package org.watsi.domain.relations

import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Member
import org.watsi.domain.entities.Referral
import java.io.Serializable

data class EncounterWithMemberAndItemsAndForms(
    val encounter: Encounter,
    val member: Member,
    val encounterItemRelations: List<EncounterItemWithBillableAndPrice>,
    val encounterForms: List<EncounterForm>,
    val referrals: List<Referral>
) : Serializable
