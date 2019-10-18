package org.watsi.device.db.daos

import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.device.db.models.BillableWithPriceSchedulesModel
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.BillableWithPriceSchedulesModelFactory
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta

class BillableDaoTest : DaoBaseTest() {

    @Test
    fun allActiveWithPrice() {
        val billable1 =
            BillableWithPriceSchedulesModelFactory.create(billableDao, priceScheduleDao)
        val billable2 =
            BillableWithPriceSchedulesModelFactory.create(billableDao, priceScheduleDao)
        val billable3 =
            BillableWithPriceSchedulesModelFactory.create(billableDao, priceScheduleDao)
        val inactiveBillable4 =
                BillableWithPriceSchedulesModelFactory.create(
                    billableDao = billableDao,
                    priceScheduleDao = priceScheduleDao,
                    billableModel = BillableModelFactory.build(active = false)
                )

        billableDao.allActiveWithPrice().test().assertValue(listOf(billable1, billable2, billable3))
    }

    @Test
    fun countActive() {
        val billable1 =
                BillableWithPriceSchedulesModelFactory.create(billableDao, priceScheduleDao)
        val billable2 =
                BillableWithPriceSchedulesModelFactory.create(billableDao, priceScheduleDao)
        val billable3 =
                BillableWithPriceSchedulesModelFactory.create(billableDao, priceScheduleDao)
        val inactiveBillable4 =
                BillableWithPriceSchedulesModelFactory.create(
                    billableDao = billableDao,
                    priceScheduleDao = priceScheduleDao,
                    billableModel = BillableModelFactory.build(active = false)
                )

        billableDao.countActive().test().assertValue(3)
    }

    @Test
    fun allActiveIdsOfType() {
        BillableModelFactory.create(billableDao, type = Billable.Type.VACCINE)
        val drug1 = BillableModelFactory.create(billableDao, type = Billable.Type.DRUG)
        val drug2 = BillableModelFactory.create(billableDao, type = Billable.Type.DRUG)
        val drug3 = BillableModelFactory.create(billableDao, type = Billable.Type.DRUG)
        val inactiveDrug3 = BillableModelFactory.create(billableDao, type = Billable.Type.DRUG, active = false)
        val lab1 = BillableModelFactory.create(billableDao, type = Billable.Type.LAB)
        val inactiveLab2 = BillableModelFactory.create(billableDao, type = Billable.Type.LAB, active = false)
        val service1 = BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE)
        val service2 = BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE)
        val inactiveService3= BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE, active = false)

        billableDao.allActiveIdsOfType(Billable.Type.SERVICE).test().assertValue(
            listOf(service1.id, service2.id)
        )

        billableDao.allActiveIdsOfType(Billable.Type.LAB).test().assertValue(
            listOf(lab1.id)
        )

        billableDao.allActiveIdsOfType(Billable.Type.DRUG).test().assertValue(
            listOf(drug1.id, drug2.id, drug3.id)
        )
    }

    @Test
    fun unsynced() {
        val unsyncedBillable = BillableModelFactory.create(billableDao)
        val syncedBillable = BillableModelFactory.create(billableDao)
        BillableModelFactory.create(billableDao)

        DeltaModelFactory.create(deltaDao,
                modelName = Delta.ModelName.BILLABLE, modelId = unsyncedBillable.id, synced = false)
        DeltaModelFactory.create(deltaDao,
                modelName = Delta.ModelName.BILLABLE, modelId = syncedBillable.id, synced = true)

        billableDao.unsynced().test().assertValue(listOf(unsyncedBillable))
    }

    @Test
    fun opdDefaults() {
        val consultation = BillableWithPriceSchedulesModelFactory.create(
            billableDao,
            priceScheduleDao,
            BillableModelFactory.build(name = "Consultation")
        )
        val medicalForm = BillableWithPriceSchedulesModelFactory.create(
            billableDao,
            priceScheduleDao,
            BillableModelFactory.build(name = "Medical Form")
        )
        BillableWithPriceSchedulesModelFactory.create(billableDao, priceScheduleDao)

        billableDao.opdDefaults().test().assertValue(listOf(consultation, medicalForm))
    }
}
