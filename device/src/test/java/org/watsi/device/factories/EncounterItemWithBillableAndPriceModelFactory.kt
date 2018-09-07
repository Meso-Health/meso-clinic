package org.watsi.device.factories

import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.EncounterItemDao
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.BillableWithPriceScheduleListModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterItemWithBillableAndPriceModel
import java.util.*

object EncounterItemWithBillableAndPriceModelFactory {

    fun build(
        billableWithPriceScheduleListModel: BillableWithPriceScheduleListModel = BillableWithPriceScheduleListModelFactory.build(),
        encounterItemModel: EncounterItemModel = EncounterItemModelFactory.build(
            billableId = billableWithPriceScheduleListModel.billableModel?.id ?: UUID.randomUUID()
        )
    ): EncounterItemWithBillableAndPriceModel {
        return EncounterItemWithBillableAndPriceModel(
            encounterItemModel,
            listOf(billableWithPriceScheduleListModel)
        )
    }

    fun create(
        billableDao: BillableDao,
        priceScheduleDao: PriceScheduleDao,
        encounterItemDao: EncounterItemDao,
        billableWithPriceScheduleListModel: BillableWithPriceScheduleListModel = BillableWithPriceScheduleListModelFactory.build(),
        encounterItemModel: EncounterItemModel = EncounterItemModelFactory.build(
            billableId = billableWithPriceScheduleListModel.billableModel?.id ?: UUID.randomUUID()
        )
    ): EncounterItemWithBillableAndPriceModel {
        val model = build(
            billableWithPriceScheduleListModel,
            encounterItemModel
        )
        billableWithPriceScheduleListModel?.billableModel?.let { billableDao.insert(it) }
        billableWithPriceScheduleListModel?.priceScheduleModels?.firstOrNull()?.let { priceScheduleDao.insert(it) }
        encounterItemDao.insert(encounterItemModel)
        return model
    }
}
