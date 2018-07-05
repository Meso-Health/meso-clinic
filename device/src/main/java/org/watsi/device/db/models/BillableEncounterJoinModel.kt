package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import java.util.UUID


@Entity(
    tableName = "billable_encounter",
    primaryKeys = ["billableId", "encounterId"],
    indices= [Index("billableId"), Index("encounterId")],
    foreignKeys = [
        ForeignKey(
            entity = BillableModel::class,
            parentColumns = ["id"],
            childColumns= ["billableId"]
        ),
        ForeignKey(
            entity = EncounterModel::class,
            parentColumns = ["id"],
            childColumns= ["encounterId"]
        )
    ]
)
data class BillableEncounterJoinModel(val billableId: UUID, val encounterId: UUID)
