package org.watsi.domain.factories

import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Referral
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithItemsAndForms

object EncounterWithItemsAndFormsFactory {
    fun build(encounter: Encounter = EncounterFactory.build(),
              encounterItemRelations: List<EncounterItemWithBillableAndPrice> = emptyList(),
              forms: List<EncounterForm> = emptyList(),
              referral: Referral? = null
    ) : EncounterWithItemsAndForms {
        return EncounterWithItemsAndForms(
            encounter = encounter,
            encounterItemRelations = encounterItemRelations,
            encounterForms = forms,
            referral = referral
        )
    }
}
