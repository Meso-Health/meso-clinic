package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice

data class EncounterItemWithBillableAndPriceModel(
    @Embedded var encounterItemModel: EncounterItemModel? = null,
    @Relation(parentColumn = "priceScheduleId", entityColumn = "id", entity = PriceScheduleModel::class)
    var priceScheduleWithBillableModel: List<PriceScheduleWithBillableModel>? = null
) {
    fun toEncounterItemWithBillableAndPrice(): EncounterItemWithBillableAndPrice {
        encounterItemModel?.toEncounterItem()?.let { encounterItem ->
            priceScheduleWithBillableModel?.firstOrNull()?.toBillableWithPriceSchedule()
                ?.let { billableWithPriceSchedule ->
                    return EncounterItemWithBillableAndPrice(
                        encounterItem,
                        billableWithPriceSchedule
                    )
                }
            throw IllegalStateException("BillableModel cannot be null")
        }
        throw IllegalStateException("EncounterItemModel cannot be null")
    }
}
