package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Instant
import java.util.UUID

@Entity(tableName = "price_schedules")
data class PriceScheduleModel(
        @PrimaryKey val id: UUID,
        val createdAt: Instant,
        val updatedAt: Instant,
        val issuedAt: Instant,
        val billableId: UUID,
        val price: Int,
        val previousPriceScheduleId: UUID?
)
