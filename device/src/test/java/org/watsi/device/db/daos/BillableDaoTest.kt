package org.watsi.device.db.daos

import org.junit.Test
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.device.factories.PriceScheduleModelFactory
import org.watsi.domain.entities.Billable
import org.watsi.domain.entities.Delta

class BillableDaoTest : DaoBaseTest() {

    @Test
    fun ofType() {
        val service1 = BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE)
        val service2 = BillableModelFactory.create(billableDao, type = Billable.Type.SERVICE)
        BillableModelFactory.create(billableDao, type = Billable.Type.DRUG)

        billableDao.ofType(Billable.Type.SERVICE).test().assertValue(listOf(service1, service2))
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
        val consultation = BillableModelFactory.create(billableDao, name = "Consultation")
        val medicalForm = BillableModelFactory.create(billableDao, name = "Medical Form")
        BillableModelFactory.create(billableDao)

        billableDao.opdDefaults().test().assertValue(listOf(consultation, medicalForm))
    }
}
