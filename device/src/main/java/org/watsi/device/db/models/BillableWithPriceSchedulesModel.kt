package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.BillableWithPriceSchedules

data class BillableWithPriceSchedulesModel(
    @Embedded var billableModel: BillableModel? = null,
    @Relation(parentColumn = "id", entityColumn = "billableId", entity = PriceScheduleModel::class)
    var priceScheduleModels: List<PriceScheduleModel>? = null
) {
    fun toBillableWithPriceSchedules(): BillableWithPriceSchedules {
        billableModel?.toBillable()?.let { billable ->
            priceScheduleModels?.let { priceScheduleModels ->
                return BillableWithPriceSchedules(
                    billable,
                    priceScheduleModels.map { it.toPriceSchedule() }
                )
            }
            throw IllegalStateException("PriceScheduleModel cannot be null or empty. BillableId: " + billable.id)
        }
        throw IllegalStateException("BillableModel cannot be null")
    }
}
