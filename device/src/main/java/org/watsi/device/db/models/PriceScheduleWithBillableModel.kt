package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.BillableWithPriceSchedule

data class PriceScheduleWithBillableModel(
    @Embedded var priceScheduleModel: PriceScheduleModel? = null,
    @Relation(parentColumn = "billableId", entityColumn = "id", entity = BillableModel::class)
    var billableModel: List<BillableModel>? = null
) {
    fun toBillableWithPriceSchedule(): BillableWithPriceSchedule {
        priceScheduleModel?.toPriceSchedule()?.let { priceSchedule ->
            billableModel?.firstOrNull()?.toBillable()?.let { billable ->
                return BillableWithPriceSchedule(billable, priceSchedule)
            }
            throw IllegalStateException("BillableModel cannot be null")
        }
        throw IllegalStateException("PriceScheduleModel cannot be null")
    }
}
