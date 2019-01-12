package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.PriceScheduleDao
import org.watsi.device.db.models.PriceScheduleModel
import java.util.UUID

object PriceScheduleModelFactory {
    fun build(
            id: UUID = UUID.randomUUID(),
            issuedAt: Instant = Instant.now(),
            billableId: UUID = UUID.randomUUID(),
            price: Int = 10,
            previousPriceScheduleModelId: UUID? = null,
            createdAt: Instant? = null,
            updatedAt: Instant? = null,
            clock: Clock = Clock.systemUTC()
    ): PriceScheduleModel {
        val now = Instant.now(clock)
        return PriceScheduleModel(
            id = id,
            issuedAt = issuedAt,
            billableId = billableId,
            price = price,
            previousPriceScheduleId = previousPriceScheduleModelId,
            createdAt = now,
            updatedAt = now
        )
    }

    fun create(
        priceScheduleDao: PriceScheduleDao,
        id: UUID = UUID.randomUUID(),
        issuedAt: Instant = Instant.now(),
        billableId: UUID = UUID.randomUUID(),
        price: Int = 10,
        previousPriceScheduleModelId: UUID? = null,
        createdAt: Instant? = null,
        updatedAt: Instant? = null,
        clock: Clock = Clock.systemUTC()
    ): PriceScheduleModel {
        val model = build(
            id = id,
            issuedAt = issuedAt,
            billableId = billableId,
            price = price,
            previousPriceScheduleModelId = previousPriceScheduleModelId,
            createdAt = createdAt,
            updatedAt = updatedAt,
            clock = clock
        )
        priceScheduleDao.insert(model)
        return model
    }
}
