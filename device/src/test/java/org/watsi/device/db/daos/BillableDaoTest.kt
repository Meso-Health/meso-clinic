package org.watsi.device.db.daos

import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.device.db.models.BillableWithPriceScheduleListModel
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.BillableWithPriceScheduleListModelFactory
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta

class BillableDaoTest : DaoBaseTest() {

    @Test
    fun allWithPrice() {
        val billable1 =
            BillableWithPriceScheduleListModelFactory.create(billableDao, priceScheduleDao)
        val billable2 =
            BillableWithPriceScheduleListModelFactory.create(billableDao, priceScheduleDao)
        val billable3 =
            BillableWithPriceScheduleListModelFactory.create(billableDao, priceScheduleDao)

        billableDao.allWithPrice().test().assertValue(listOf(billable1, billable2, billable3))
    }

    @Test
    fun ofType() {
        val service1 = BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE)
        val service2 = BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE)
        BillableModelFactory.create(billableDao, type = Billable.Type.DRUG)

        billableDao.ofType(Billable.Type.SERVICE).test().assertValue(listOf(service1, service2))
    }

    @Test
    fun ofTypeWithPrice() {
        val oldDate = Instant.ofEpochMilli(1533090767000) // 2018/08/01
        val middleDate = Instant.ofEpochMilli(1534300367000) // 2018/08/15
        val newDate = Instant.ofEpochMilli(1535769167000) // 2018/09/01

        val vaccine1 = BillableModelFactory.create(billableDao, type = Billable.Type.VACCINE)
        val drug1 = BillableModelFactory.create(billableDao, type = Billable.Type.DRUG)
        val drug2 = BillableModelFactory.create(billableDao, type = Billable.Type.DRUG)
        val drug3 = BillableModelFactory.create(billableDao, type = Billable.Type.DRUG)
        val lab1 = BillableModelFactory.create(billableDao, type = Billable.Type.LAB)
        val service1 = BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE)
        val service2 = BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE)

        val priceScheduleVaccine1 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = vaccine1.id,
            issuedAt = oldDate
        )
        val priceScheduleDrug1_1 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = drug1.id,
            issuedAt = oldDate
        )
        val priceScheduleDrug1_2 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = drug1.id,
            issuedAt = middleDate,
            previousPriceScheduleModelId = priceScheduleDrug1_1.id
        )
        val priceScheduleDrug2_1 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = drug2.id,
            issuedAt = oldDate
        )
        val priceScheduleDrug2_2 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = drug2.id,
            issuedAt = middleDate,
            previousPriceScheduleModelId = priceScheduleDrug2_1.id
        )
        val priceScheduleDrug2_3 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = drug2.id,
            issuedAt = newDate,
            previousPriceScheduleModelId = priceScheduleDrug2_2.id
        )
        val priceScheduleDrug3 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = drug3.id,
            issuedAt = oldDate
        )
        val priceScheduleLab1_1 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = lab1.id,
            issuedAt = oldDate
        )
        val priceScheduleLab1_2 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = lab1.id,
            issuedAt = middleDate,
            previousPriceScheduleModelId = priceScheduleLab1_1.id
        )
        val priceScheduleService1 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = service1.id,
            issuedAt = oldDate
        )
        val priceScheduleService2 = PriceScheduleModelFactory.create(
            priceScheduleDao,
            billableId = service2.id,
            issuedAt = oldDate
        )

        billableDao.ofTypeWithPrice(Billable.Type.SERVICE).test().assertValue(
            listOf(
                BillableWithPriceScheduleListModel(service1, listOf(priceScheduleService1)),
                BillableWithPriceScheduleListModel(service2, listOf(priceScheduleService2))
            )
        )

        billableDao.ofTypeWithPrice(Billable.Type.LAB).test().assertValue(
            listOf(
                BillableWithPriceScheduleListModel(
                    lab1,
                    listOf(priceScheduleLab1_1, priceScheduleLab1_2)
                )
            )
        )

        billableDao.ofTypeWithPrice(Billable.Type.DRUG).test().assertValue(
            listOf(
                BillableWithPriceScheduleListModel(
                    drug1,
                    listOf(priceScheduleDrug1_1, priceScheduleDrug1_2)
                ),
                BillableWithPriceScheduleListModel(
                    drug2,
                    listOf(priceScheduleDrug2_1, priceScheduleDrug2_2, priceScheduleDrug2_3)
                ),
                BillableWithPriceScheduleListModel(drug3, listOf(priceScheduleDrug3))
            )
        )
    }

    @Test
    fun upsert() {
        val persistedBillable = BillableModelFactory.create(billableDao)
        val persistedPriceSchedule = PriceScheduleModelFactory.create(priceScheduleDao, billableId = persistedBillable.id)
        val newPriceScheduleForPersistedBillable = PriceScheduleModelFactory.create(priceScheduleDao, billableId = persistedBillable.id)
        val newBillable = BillableModelFactory.build()
        val priceScheduleForNewBillable = PriceScheduleModelFactory.create(priceScheduleDao, billableId = newBillable.id)
        val updatedBillable = persistedBillable.copy(price = 500)

        billableDao.upsert(
            billableModels = listOf(updatedBillable, newBillable),
            priceScheduleModels = listOf(
                persistedPriceSchedule,
                newPriceScheduleForPersistedBillable,
                priceScheduleForNewBillable
            )
        )

        billableDao.all().test().assertValue(listOf(
            updatedBillable,
            newBillable
        ))

        priceScheduleDao.all().test().assertValue(listOf(
            persistedPriceSchedule,
            newPriceScheduleForPersistedBillable,
            priceScheduleForNewBillable
        ))
    }

    @Test
    fun distinctCompositions() {
        BillableModelFactory.create(billableDao, composition = "foo")
        BillableModelFactory.create(billableDao, composition = "foo")
        BillableModelFactory.create(billableDao, composition = "bar")
        BillableModelFactory.create(billableDao, composition = null)

        billableDao.distinctCompositions().test().assertValue(listOf("foo", "bar"))
    }

    @Test
    fun delete() {
        val model1 = BillableModelFactory.create(billableDao)
        val model2 = BillableModelFactory.create(billableDao)

        billableDao.delete(listOf(model1.id))

        billableDao.all().test().assertValue(listOf(model2))
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
        val consultation = BillableWithPriceScheduleListModelFactory.create(
            billableDao,
            priceScheduleDao,
            BillableModelFactory.build(name = "Consultation")
        )
        val medicalForm = BillableWithPriceScheduleListModelFactory.create(
            billableDao,
            priceScheduleDao,
            BillableModelFactory.build(name = "Medical Form")
        )
        BillableWithPriceScheduleListModelFactory.create(billableDao, priceScheduleDao)

        billableDao.opdDefaultsWithPrice().test().assertValue(listOf(consultation, medicalForm))
    }
}
