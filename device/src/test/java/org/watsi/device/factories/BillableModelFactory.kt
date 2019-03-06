package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.BillableDao
import org.watsi.device.db.models.BillableModel
import org.watsi.domain.entities.Billable
import java.util.UUID

object BillableModelFactory {

    fun build(
            id: UUID = UUID.randomUUID(),
            type: Billable.Type = Billable.Type.SERVICE,
            composition: String? = null,
            unit: String? = null,
            name: String = "Delivery",
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            clock: Clock = Clock.systemUTC()) : BillableModel {
        val now = Instant.now(clock)
        return BillableModel(id = id,
                             type = type,
                             composition = composition,
                             unit = unit,
                             name = name,
                             createdAt = createdAt ?: now,
                             updatedAt = updatedAt ?: now)
    }

    fun create(billableDao: BillableDao,
               id: UUID = UUID.randomUUID(),
               type: Billable.Type = Billable.Type.SERVICE,
               composition: String? = null,
               unit: String? = null,
               name: String = "Delivery",
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               clock: Clock = Clock.systemUTC()) : BillableModel {
        val model = build(id = id,
                          type = type,
                          composition = composition,
                          unit = unit,
                          name = name,
                          createdAt = createdAt,
                          updatedAt = updatedAt,
                          clock = clock)
        billableDao.insert(model)
        return model
    }
}
