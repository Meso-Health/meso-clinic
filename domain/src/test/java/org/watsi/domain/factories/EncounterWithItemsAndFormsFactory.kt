package org.watsi.domain.factories

import org.watsi.domain.entities.Diagnosis
import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.relations.EncounterItemWithBillable
import org.watsi.domain.relations.EncounterWithItemsAndForms

object EncounterWithItemsAndFormsFactory {
    fun build(encounter: Encounter = EncounterFactory.build(),
              items: List<EncounterItemWithBillable> = emptyList(),
              forms: List<EncounterForm> = emptyList(),
              diagnoses: List<Diagnosis> = emptyList()
    ) : EncounterWithItemsAndForms {
        return EncounterWithItemsAndForms(encounter, items, forms, diagnoses)
    }
}
