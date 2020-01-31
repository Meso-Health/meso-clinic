package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.watsi.domain.entities.LabResult
import java.util.UUID

@Entity(
    tableName = "lab_results",
    indices = [
        Index("encounterItemId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = EncounterItemModel::class,
            parentColumns = ["id"],
            childColumns = ["encounterItemId"]
        )
    ]
)
data class LabResultModel(
    @PrimaryKey val id: UUID,
    val result: String,
    val encounterItemId: UUID
) {
    companion object {
        fun fromLabResult(labResult: LabResult) : LabResultModel {
            return LabResultModel(
                id = labResult.id,
                result = labResult.result,
                encounterItemId = labResult.encounterItemId
            )
        }
    }

    fun toLabResult(): LabResult {
        return LabResult(
            id = id,
            result = result,
            encounterItemId = encounterItemId
        )
    }
}
