package org.watsi.device.factories

import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.BillableWithPriceScheduleListModel
import org.watsi.device.db.models.PriceScheduleModel

object BillableWithPriceScheduleListModelFactory {

    fun build(
        billableModel: BillableModel = BillableModelFactory.build(),
        priceScheduleModelList: List<PriceScheduleModel> = listOf(
            PriceScheduleModelFactory.build(
                billableId = billableModel.id
            )
        )
    ): BillableWithPriceScheduleListModel {
        return BillableWithPriceScheduleListModel(
            billableModel,
            priceScheduleModelList
        )
    }

    fun create(
        billableDao: BillableDao,
        priceScheduleDao: PriceScheduleDao,
        billableModel: BillableModel = BillableModelFactory.build(),
        priceScheduleModelList: List<PriceScheduleModel> = listOf(
            PriceScheduleModelFactory.build(
                billableId = billableModel.id
            )
        )
    ): BillableWithPriceScheduleListModel {
        val model = build(
            billableModel,
            priceScheduleModelList
        )
        billableDao.insert(billableModel)
        priceScheduleModelList.forEach { priceScheduleModel ->
            priceScheduleDao.insert(priceScheduleModel)
        }
        return model
    }
}
