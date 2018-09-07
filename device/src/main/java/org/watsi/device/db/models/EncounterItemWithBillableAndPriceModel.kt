package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterItemWithBillableAndPriceList

data class EncounterItemWithBillableAndPriceModel(
    @Embedded var encounterItemModel: EncounterItemModel? = null,
    @Relation(parentColumn = "billableId", entityColumn = "id", entity = BillableModel::class)
    var billableModel: List<BillableWithPriceScheduleListModel>? = null
) {
    fun toEncounterItemWithBillableAndPrice(): EncounterItemWithBillableAndPriceList {
        encounterItemModel?.toEncounterItem()?.let { encounterItem ->
            billableModel?.firstOrNull()?.toBillableWithPriceScheduleList()
                ?.let { billableWithPriceScheduleList ->
                    return EncounterItemWithBillableAndPriceList(
                        encounterItem,
                        billableWithPriceScheduleList
                    )
                }
            throw IllegalStateException("BillableModel cannot be null")
        }
        throw IllegalStateException("EncounterItemModel cannot be null")
    }
}
