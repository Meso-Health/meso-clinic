package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.EncounterItemDao
import org.watsi.device.db.models.EncounterItemModel
import java.util.UUID

object EncounterItemModelFactory {

    fun build(
        id: UUID = UUID.randomUUID(),
        encounterId: UUID = UUID.randomUUID(),
        billableId: UUID = UUID.randomUUID(),
        quantity: Int = 1,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        priceScheduleId: UUID = UUID.randomUUID(),
        priceScheduleIssued: Boolean = false,
        clock: Clock = Clock.systemUTC()
    ): EncounterItemModel {
        val now = Instant.now(clock)
        return EncounterItemModel(
            id = id,
            encounterId = encounterId,
            billableId = billableId,
            quantity = quantity,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now,
            priceScheduleId = priceScheduleId,
            priceScheduleIssued = priceScheduleIssued
        )
    }

    fun create(
        encounterItemDao: EncounterItemDao,
        id: UUID = UUID.randomUUID(),
        encounterId: UUID = UUID.randomUUID(),
        billableId: UUID = UUID.randomUUID(),
        quantity: Int = 1,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        priceScheduleId: UUID = UUID.randomUUID(),
        priceScheduleIssued: Boolean = false,
        clock: Clock = Clock.systemUTC()
    ): EncounterItemModel {
        val model = build(
            id = id,
            encounterId = encounterId,
            billableId = billableId,
            quantity = quantity,
            createdAt = createdAt,
            updatedAt = updatedAt,
            priceScheduleId = priceScheduleId,
            priceScheduleIssued = priceScheduleIssued,
            clock = clock
        )
        encounterItemDao.insert(model)
        return model
    }
}
