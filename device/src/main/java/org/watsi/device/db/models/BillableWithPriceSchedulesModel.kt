package org.watsi.device.db.models

import android.arch.persistence.room.Embedded
import android.arch.persistence.room.Relation
import org.watsi.domain.relations.BillableWithPriceSchedule

data class BillableWithPriceSchedulesModel(
    @Embedded var billableModel: BillableModel? = null,
    @Relation(parentColumn = "id", entityColumn = "billableId", entity = PriceScheduleModel::class)
    var priceScheduleModels: List<PriceScheduleModel>? = null
) {
    fun toBillableWithCurrentPriceSchedule(): BillableWithPriceSchedule {
        billableModel?.toBillable()?.let { billable ->
            priceScheduleModels?.let { priceScheduleModels ->
                priceScheduleModels.maxBy { it.issuedAt }?.let {
                    return BillableWithPriceSchedule(billable, it.toPriceSchedule())
                }
                throw IllegalStateException("Failed to find current PriceSchedule. BillableId: " + billable.id)
            }
            throw IllegalStateException("PriceScheduleModel cannot be null or empty. BillableId: " + billable.id)
        }
        throw IllegalStateException("BillableModel cannot be null")
    }
}
