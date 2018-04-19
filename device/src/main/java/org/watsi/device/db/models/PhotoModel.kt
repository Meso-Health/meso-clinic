package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Photo
import java.util.UUID

@Entity(tableName = "photos")
data class PhotoModel(val id: UUID,
                      val createdAt: Instant,
                      val updatedAt: Instant,
                      val url: String,
                      val deleted: Boolean) {

    fun toPhoto(): Photo {
        return Photo(id = id, url = url, deleted = deleted)
    }

    companion object {
        fun fromPhoto(photo: Photo, clock: Clock): PhotoModel {
            val now = clock.instant()
            return PhotoModel(id = photo.id,
                              createdAt = now,
                              updatedAt = now,
                              url = photo.url,
                              deleted = photo.deleted)
        }
    }
}
