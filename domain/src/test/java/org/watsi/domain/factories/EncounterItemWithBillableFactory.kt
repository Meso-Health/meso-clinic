package org.watsi.domain.factories

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import org.watsi.domain.relations.EncounterItemWithBillable

object EncounterItemWithBillableFactory {
    fun build(billable: Billable = BillableFactory.build(),
              encounterItem: EncounterItem = EncounterItemFactory.build(billableId = billable.id)
    ) : EncounterItemWithBillable {
        return EncounterItemWithBillable(encounterItem, billable)
    }
}
