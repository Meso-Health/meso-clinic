package org.watsi.domain.factories

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice
import org.watsi.domain.relations.EncounterWithItemsAndForms

object EncounterWithItemsAndFormsFactory {
    fun build(encounter: Encounter = EncounterFactory.build(),
              encounterItemRelations: List<EncounterItemWithBillableAndPrice> = emptyList(),
              forms: List<EncounterForm> = emptyList(),
              diagnoses: List<Diagnosis> = emptyList()
    ) : EncounterWithItemsAndForms {
        return EncounterWithItemsAndForms(encounter, encounterItemRelations, forms, diagnoses)
    }
}
