package org.watsi.device.db.daos

import org.junit.Test
import org.watsi.device.factories.DeltaModelFactory
import org.watsi.domain.entities.Delta
import java.util.UUID


class DeltaDaoTest : DaoBaseTest() {

    @Test
    fun unsyncedCount() {
        val memberID = UUID.randomUUID()
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD, modelName = Delta.ModelName.MEMBER, modelId = memberID, synced = false)
        DeltaModelFactory.create(deltaDao, action = Delta.Action.EDIT, modelName = Delta.ModelName.MEMBER, modelId = memberID, synced = false)
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD, modelName = Delta.ModelName.MEMBER, synced = false)
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD, modelName = Delta.ModelName.MEMBER, synced = true)
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD, modelName = Delta.ModelName.ENCOUNTER, synced = false)
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD, modelName = Delta.ModelName.ENCOUNTER_FORM, synced = false)
        DeltaModelFactory.create(deltaDao, action = Delta.Action.ADD, modelName = Delta.ModelName.ENCOUNTER_FORM, synced = false)


        deltaDao.unsyncedCount(Delta.ModelName.MEMBER).test().assertValue(2)
        deltaDao.unsyncedCount(Delta.ModelName.ENCOUNTER_FORM).test().assertValue(2)
        deltaDao.unsyncedCount(Delta.ModelName.MEMBER, Delta.Action.EDIT).test().assertValue(1)
        deltaDao.unsyncedCount(Delta.ModelName.MEMBER, Delta.Action.ADD).test().assertValue(2)
        deltaDao.unsyncedCount(Delta.ModelName.IDENTIFICATION_EVENT).test().assertValue(0)
        deltaDao.unsyncedCount(Delta.ModelName.ENCOUNTER_FORM, Delta.Action.EDIT).test().assertValue(0)
    }
}
