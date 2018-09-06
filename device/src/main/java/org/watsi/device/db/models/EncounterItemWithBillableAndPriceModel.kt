package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.BillableWithPriceSchedule
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice

data class EncounterItemWithBillableAndPriceModel(
    @Embedded var encounterItemModel: EncounterItemModel? = null,
    @Relation(parentColumn = "billableId", entityColumn = "id", entity = BillableModel::class)
    var billableModel: List<BillableModel>? = null,
    @Relation(parentColumn = "priceScheduleId", entityColumn = "id", entity = PriceScheduleModel::class)
    var priceScheduleModel: List<PriceScheduleModel>? = null
) {
    fun toEncounterItemWithBillableAndPrice(): EncounterItemWithBillableAndPrice {
        encounterItemModel?.toEncounterItem()?.let { encounterItem ->
            priceScheduleModel?.firstOrNull()?.toPriceSchedule()?.let { priceSchedule ->
                billableModel?.firstOrNull()?.toBillable()?.let { billable ->
                    return EncounterItemWithBillableAndPrice(
                        encounterItem,
                        BillableWithPriceSchedule(billable, priceSchedule)
                    )
                }
                throw IllegalStateException("BillableModel cannot be null")
            }
            throw IllegalStateException("PriceScheduleModel cannot be null")
        }
        throw IllegalStateException("EncounterItemModel cannot be null")
    }
}
