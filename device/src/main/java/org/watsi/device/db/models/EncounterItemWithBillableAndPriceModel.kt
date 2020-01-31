package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.EncounterItemWithBillableAndPrice

data class EncounterItemWithBillableAndPriceModel(
    @Embedded var encounterItemModel: EncounterItemModel? = null,
    @Relation(parentColumn = "priceScheduleId", entityColumn = "id", entity = PriceScheduleModel::class)
    var priceScheduleWithBillableModel: List<PriceScheduleWithBillableModel>? = null,
    @Relation(parentColumn = "id", entityColumn = "encounterItemId", entity = LabResultModel::class)
    var labResultModel: List<LabResultModel>? = null
) {
    fun toEncounterItemWithBillableAndPrice(): EncounterItemWithBillableAndPrice {
        encounterItemModel?.toEncounterItem()?.let { encounterItem ->
            priceScheduleWithBillableModel?.firstOrNull()?.toBillableWithPriceSchedule()
                ?.let { billableWithPriceSchedule ->
                    return EncounterItemWithBillableAndPrice(
                        encounterItem,
                        billableWithPriceSchedule,
                        labResult = labResultModel?.firstOrNull()?.toLabResult()
                    )
                }
            throw IllegalStateException("BillableModel cannot be null")
        }
        throw IllegalStateException("EncounterItemModel cannot be null")
    }
}
