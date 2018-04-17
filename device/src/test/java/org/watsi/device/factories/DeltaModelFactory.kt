package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.DeltaDao
import org.watsi.device.db.models.DeltaModel
import org.watsi.domain.entities.Delta
import java.util.UUID

object DeltaModelFactory {
    fun build(id: Int = 0,
              action: Delta.Action = Delta.Action.ADD,
              modelName: Delta.ModelName = Delta.ModelName.MEMBER,
              synced: Boolean = false,
              modelId: UUID = UUID.randomUUID(),
              field: String? = null,
              createdAt: Instant? = null,
              updatedAt: Instant? = null,
              clock: Clock = Clock.systemUTC()
    ): DeltaModel {
        val currentTime = Instant.now(clock)
        return DeltaModel(id = id,
                action = action,
                modelName = modelName,
                modelId = modelId,
                field = field,
                synced = synced,
                createdAt = createdAt ?: currentTime,
                updatedAt = updatedAt ?: currentTime)
    }

    fun create(deltaDao: DeltaDao,
               id: Int = 0,
               action: Delta.Action = Delta.Action.ADD,
               modelName: Delta.ModelName = Delta.ModelName.MEMBER,
               synced: Boolean = false,
               modelId: UUID = UUID.randomUUID(),
               field: String? = null,
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               clock: Clock = Clock.systemUTC()
    ): DeltaModel {
        val deltaModel = build(id = id,
                action = action,
                modelName = modelName,
                synced = synced,
                modelId = modelId,
                field = field,
                createdAt = createdAt,
                updatedAt = updatedAt,
                clock = clock)
        deltaDao.insert(deltaModel)
        return deltaModel
    }
}
