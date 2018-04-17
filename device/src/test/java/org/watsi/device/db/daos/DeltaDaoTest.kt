package org.watsi.device.db.daos

import org.junit.Test
import org.threeten.bp.Instant
import org.watsi.device.factories.DeltaModelFactory


class DeltaDaoTest : DaoBaseTest() {

    @Test
    fun lastSynced() {
        val now = Instant.now()
        val t1 = now.minusSeconds(3000)
        val t2 = now.minusSeconds(2000)
        val t3 = now.minusSeconds(1000)

        DeltaModelFactory.create(deltaDao, synced = true, updatedAt = t1)
        DeltaModelFactory.create(deltaDao, synced = true, updatedAt = t2)
        DeltaModelFactory.create(deltaDao, synced = false, updatedAt = t3)

        deltaDao.lastSynced().test().assertValue(t2)
    }
}
