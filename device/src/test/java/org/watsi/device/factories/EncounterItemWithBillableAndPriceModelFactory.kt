package org.watsi.device.factories

import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.EncounterItemDao
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterItemWithBillableAndPriceModel
import org.watsi.device.db.models.PriceScheduleWithBillableModel
import java.util.UUID

object EncounterItemWithBillableAndPriceModelFactory {

    fun build(
        priceScheduleWithBillableModel: PriceScheduleWithBillableModel = PriceScheduleWithBillableModelFactory.build(),
        encounterItemModel: EncounterItemModel = EncounterItemModelFactory.build(
            billableId = priceScheduleWithBillableModel.billableModel?.firstOrNull()?.id ?: UUID.randomUUID()
        )
    ): EncounterItemWithBillableAndPriceModel {
        return EncounterItemWithBillableAndPriceModel(
            encounterItemModel,
            listOf(priceScheduleWithBillableModel)
        )
    }

    fun create(
        billableDao: BillableDao,
        priceScheduleDao: PriceScheduleDao,
        encounterItemDao: EncounterItemDao,
        priceScheduleWithBillableModel: PriceScheduleWithBillableModel = PriceScheduleWithBillableModelFactory.build(),
        encounterItemModel: EncounterItemModel = EncounterItemModelFactory.build(
            billableId = priceScheduleWithBillableModel.billableModel?.firstOrNull()?.id ?: UUID.randomUUID()
        )
    ): EncounterItemWithBillableAndPriceModel {
        val model = build(
            priceScheduleWithBillableModel,
            encounterItemModel
        )
        priceScheduleWithBillableModel?.billableModel?.firstOrNull()?.let { billableDao.insert(it) }
        priceScheduleWithBillableModel?.priceScheduleModel?.let { priceScheduleDao.insert(it) }
        encounterItemDao.insert(encounterItemModel)
        return model
    }
}
