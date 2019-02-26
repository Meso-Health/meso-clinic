package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Billable
import java.util.UUID

@Entity(tableName = "billables")
data class BillableModel(@PrimaryKey val id: UUID,
                         val createdAt: Instant,
                         val updatedAt: Instant,
                         val type: Billable.Type,
                         val composition: String?,
                         val unit: String?,
                         val name: String) {

    fun toBillable(): Billable {
        return Billable(id = id,
                        type = type,
                        composition = composition,
                        unit = unit,
                        name = name)
    }

    companion object {
        fun fromBillable(billable: Billable, clock: Clock): BillableModel {
            val now = clock.instant()
            return BillableModel(id = billable.id,
                                 createdAt = now,
                                 updatedAt = now,
                                 type = billable.type,
                                 composition = billable.composition,
                                 unit = billable.unit,
                                 name = billable.name)
        }
    }
}
