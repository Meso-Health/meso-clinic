package org.watsi.domain.relations

import org.watsi.domain.entities.EncounterItem
import java.io.Serializable

data class EncounterItemWithBillableAndPriceList(
    val encounterItem: EncounterItem,
    val billableWithPriceScheduleList: BillableWithPriceScheduleList
) : Serializable {

    fun toEncounterItemWithBillableAndCurrentPrice(): EncounterItemWithBillableAndPrice {
        return EncounterItemWithBillableAndPrice(
            encounterItem,
            billableWithPriceScheduleList.toCurrentBillableWithPrice()
        )
    }
}
