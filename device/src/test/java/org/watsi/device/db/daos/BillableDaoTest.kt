package org.watsi.device.db.daos

import org.junit.Test
import org.watsi.device.factories.BillableModelFactory
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.domain.entities.Delta

class BillableDaoTest : DaoBaseTest() {

    @Test
    fun upsert() {
        val persistedBillable = BillableModelFactory.create(billableDao)
        val newBillable = BillableModelFactory.build()
        val updatedBillable = persistedBillable.copy(price = 500)

        billableDao.upsert(listOf(updatedBillable, newBillable))

        billableDao.all().test().assertValue(listOf(updatedBillable, newBillable))
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
