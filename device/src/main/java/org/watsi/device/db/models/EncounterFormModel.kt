package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.EncounterForm
import java.util.UUID

@Entity(tableName = "encounter_forms",
    indices = [
        Index("photoId"),
        Index("thumbnailId"),
        Index("encounterId")
    ],
    foreignKeys = [
        ForeignKey(
            entity = PhotoModel::class,
            parentColumns = ["id"],
            childColumns = ["photoId"]
        ),
        ForeignKey(
            entity = PhotoModel::class,
            parentColumns = ["id"],
            childColumns = ["thumbnailId"]
        ),
        ForeignKey(
            entity = EncounterModel::class,
            parentColumns = ["id"],
            childColumns = ["encounterId"]
        )
    ]
)
data class EncounterFormModel(@PrimaryKey val id: UUID,
                              val createdAt: Instant,
                              val updatedAt: Instant,
                              val encounterId: UUID,
                              val photoId: UUID?,
                              val thumbnailId: UUID?) {

    fun toEncounterForm(): EncounterForm {
        return EncounterForm(id = id, encounterId = encounterId, photoId = photoId, thumbnailId = thumbnailId)
    }

    companion object {
        fun fromEncounterForm(encounterForm: EncounterForm, clock: Clock): EncounterFormModel {
            val now = clock.instant()
            return EncounterFormModel(id = encounterForm.id,
                                      createdAt = now,
                                      updatedAt = now,
                                      encounterId = encounterForm.encounterId,
                                      photoId = encounterForm.photoId,
                                      thumbnailId = encounterForm.thumbnailId)

        }
    }
}
