package org.watsi.domain.relations

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem

data class EncounterItemWithBillable(val encounterItem: EncounterItem, val billable: Billable)
