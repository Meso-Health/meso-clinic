package org.watsi.device.factories

import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.PriceScheduleModel
import org.watsi.device.db.models.PriceScheduleWithBillableModel

object PriceScheduleWithBillableModelFactory {
    fun build(
        billableModel: BillableModel = BillableModelFactory.build(),
        priceScheduleModel: PriceScheduleModel = PriceScheduleModelFactory.build(billableId = billableModel.id)
    ): PriceScheduleWithBillableModel {
        return PriceScheduleWithBillableModel(priceScheduleModel, listOf(billableModel))
    }

    fun create(
        priceScheduleDao: PriceScheduleDao,
        billableDao: BillableDao,
        billableModel: BillableModel = BillableModelFactory.build(),
        priceScheduleModel: PriceScheduleModel = PriceScheduleModelFactory.build(billableId = billableModel.id)
    ): PriceScheduleWithBillableModel {
        val model = build(
            billableModel,
            priceScheduleModel
        )
        billableDao.insert(billableModel)
        priceScheduleDao.insert(priceScheduleModel)
        return model
    }
}
