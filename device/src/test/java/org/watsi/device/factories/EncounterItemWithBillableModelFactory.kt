package org.watsi.device.factories

import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.daos.EncounterItemDao
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterItemWithBillableModel

object EncounterItemWithBillableModelFactory {

    fun build(
        billableModel: BillableModel = BillableModelFactory.build(),
        encounterItemModel: EncounterItemModel = EncounterItemModelFactory.build(billableId = billableModel.id)
    ) : EncounterItemWithBillableModel {
        return EncounterItemWithBillableModel(
            encounterItemModel,
            listOf(billableModel)
        )
    }

    fun create(
        billableDao: BillableDao,
        encounterItemDao: EncounterItemDao,
        billableModel: BillableModel = BillableModelFactory.build(),
        encounterItemModel: EncounterItemModel = EncounterItemModelFactory.build(billableId = billableModel.id)
    ) : EncounterItemWithBillableModel {
        val model = build(
            billableModel,
            encounterItemModel
        )
        billableDao.insert(billableModel)
        encounterItemDao.insert(encounterItemModel)
        return model
    }
}
