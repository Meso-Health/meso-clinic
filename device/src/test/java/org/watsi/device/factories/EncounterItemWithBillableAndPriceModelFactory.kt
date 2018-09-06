package org.watsi.device.factories

import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.EncounterItemDao
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterItemWithBillableAndPriceModel
import org.watsi.device.db.models.PriceScheduleModel

object EncounterItemWithBillableAndPriceModelFactory {

    fun build(
        billableModel: BillableModel = BillableModelFactory.build(),
        priceScheduleModel: PriceScheduleModel = PriceScheduleModelFactory.build(billableId = billableModel.id),
        encounterItemModel: EncounterItemModel = EncounterItemModelFactory.build(billableId = billableModel.id)
    ) : EncounterItemWithBillableAndPriceModel {
        return EncounterItemWithBillableAndPriceModel(
            encounterItemModel,
            listOf(billableModel),
            listOf(priceScheduleModel)
        )
    }

    fun create(
        billableDao: BillableDao,
        priceScheduleDao: PriceScheduleDao,
        encounterItemDao: EncounterItemDao,
        billableModel: BillableModel = BillableModelFactory.build(),
        priceScheduleModel: PriceScheduleModel = PriceScheduleModelFactory.build(billableId = billableModel.id),
        encounterItemModel: EncounterItemModel = EncounterItemModelFactory.build(billableId = billableModel.id)
    ) : EncounterItemWithBillableAndPriceModel {
        val model = build(
            billableModel,
            priceScheduleModel,
            encounterItemModel
        )
        billableDao.insert(billableModel)
        priceScheduleDao.insert(priceScheduleModel)
        encounterItemDao.insert(encounterItemModel)
        return model
    }
}
