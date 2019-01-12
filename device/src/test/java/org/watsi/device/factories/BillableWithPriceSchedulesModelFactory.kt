package org.watsi.device.factories

import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.BillableWithPriceSchedulesModel
import org.watsi.device.db.models.PriceScheduleModel

object BillableWithPriceSchedulesModelFactory {

    fun build(
        billableModel: BillableModel = BillableModelFactory.build(),
        priceScheduleModels: List<PriceScheduleModel> = listOf(
            PriceScheduleModelFactory.build(
                billableId = billableModel.id
            )
        )
    ): BillableWithPriceSchedulesModel {
        return BillableWithPriceSchedulesModel(
            billableModel,
            priceScheduleModels
        )
    }

    fun create(
        billableDao: BillableDao,
        priceScheduleDao: PriceScheduleDao,
        billableModel: BillableModel = BillableModelFactory.build(),
        priceScheduleModels: List<PriceScheduleModel> = listOf(
            PriceScheduleModelFactory.build(
                billableId = billableModel.id
            )
        )
    ): BillableWithPriceSchedulesModel {
        val model = build(
            billableModel,
            priceScheduleModels
        )
        billableDao.insert(billableModel)
        priceScheduleModels.forEach { priceScheduleModel ->
            priceScheduleDao.insert(priceScheduleModel)
        }
        return model
    }
}
