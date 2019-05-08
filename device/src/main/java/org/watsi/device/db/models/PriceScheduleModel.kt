package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.PriceSchedule
import java.util.UUID

@Entity(tableName = "price_schedules",
    indices = [
        Index("billableId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = BillableModel::class,
            parentColumns = ["id"],
            childColumns = ["billableId"]
        )
    ]
)
data class PriceScheduleModel(
        @PrimaryKey val id: UUID,
        val createdAt: Instant,
        val updatedAt: Instant,
        val issuedAt: Instant,
        val billableId: UUID,
        val price: Int,
        val previousPriceScheduleId: UUID?
) {
    fun toPriceSchedule(): PriceSchedule {
        return PriceSchedule(
            id = id,
            issuedAt = issuedAt,
            billableId = billableId,
            price = price,
            previousPriceScheduleModelId = previousPriceScheduleId
        )
    }

    companion object {
        fun fromPriceSchedule(priceSchedule: PriceSchedule, clock: Clock): PriceScheduleModel {
            val now = clock.instant()
            return PriceScheduleModel(id = priceSchedule.id,
                createdAt = now,
                updatedAt = now,
                issuedAt = priceSchedule.issuedAt,
                billableId = priceSchedule.billableId,
                price = priceSchedule.price,
                previousPriceScheduleId = priceSchedule.previousPriceScheduleModelId
            )
        }
    }
}
