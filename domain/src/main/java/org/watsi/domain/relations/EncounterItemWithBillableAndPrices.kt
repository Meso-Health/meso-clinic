package org.watsi.domain.relations

import org.watsi.domain.entities.EncounterItem
import java.io.Serializable

data class EncounterItemWithBillableAndPrices(
    val encounterItem: EncounterItem,
    val billableWithPriceSchedules: BillableWithPriceSchedules
) : Serializable {

    fun toEncounterItemWithBillableAndCurrentPrice(): EncounterItemWithBillableAndPrice {
        return EncounterItemWithBillableAndPrice(
            encounterItem,
            billableWithPriceSchedules.toCurrentBillableWithPrice()
        )
    }
}
