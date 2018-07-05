package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Delta
import java.util.UUID

@Entity(tableName = "deltas")
data class DeltaModel(@PrimaryKey(autoGenerate = true) val id: Int = 0,
                      val action: Delta.Action,
                      val modelName: Delta.ModelName,
                      val modelId: UUID,
                      val field: String?,
                      val synced: Boolean,
                      val createdAt: Instant,
                      val updatedAt: Instant) {

    fun toDelta(): Delta {
        return Delta(id = id,
                action = action,
                modelName = modelName,
                modelId = modelId,
                field = field,
                synced = synced)
    }

    companion object {
        fun fromDelta(delta: Delta, clock: Clock = Clock.systemDefaultZone()): DeltaModel {
            val now = clock.instant()
            return DeltaModel(id = delta.id,
                    action = delta.action,
                    modelName = delta.modelName,
                    modelId = delta.modelId,
                    field = delta.field,
                    synced = delta.synced,
                    createdAt = now,
                    updatedAt = now)
        }
    }
}
