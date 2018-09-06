package org.watsi.domain.relations

import org.watsi.domain.entities.EncounterItem
import java.io.Serializable

data class EncounterItemWithBillableAndPrice(
    val encounterItem: EncounterItem,
    val billableWithPriceSchedule: BillableWithPriceSchedule
) : Serializable {

    fun price(): Int = encounterItem.quantity * billableWithPriceSchedule.priceSchedule.price
}
