package org.watsi.domain.relations

import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.EncounterItem
import java.io.Serializable

data class EncounterItemWithBillable(val encounterItem: EncounterItem,
                                     val billable: Billable) : Serializable {

    fun price(): Int = encounterItem.quantity * billable.price
}
