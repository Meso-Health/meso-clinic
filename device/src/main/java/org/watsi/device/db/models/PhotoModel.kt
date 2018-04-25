package org.watsi.device.db.models

import android.arch.persistence.room.ColumnInfo
import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Photo
import java.util.Arrays
import java.util.Objects
import java.util.UUID

@Entity(tableName = "photos")
data class PhotoModel(@PrimaryKey val id: UUID,
                      val createdAt: Instant,
                      val updatedAt: Instant,
                      @ColumnInfo(typeAffinity = ColumnInfo.BLOB) val bytes: ByteArray?,
                      val url: String?,
                      val deleted: Boolean) {

    fun toPhoto(): Photo {
        return Photo(id = id, bytes = bytes, url = url, deleted = deleted)
    }

    override fun equals(other: Any?): Boolean{
        if (this === other) return true
        if (other?.javaClass != javaClass) return false

        other as PhotoModel

        return id == other.id &&
                createdAt == other.createdAt &&
                updatedAt == other.updatedAt &&
                Arrays.equals(bytes, other.bytes) &&
                url == other.url &&
                deleted == other.deleted
    }

    override fun hashCode(): Int{
        return Objects.hash(id, createdAt, updatedAt, Arrays.hashCode(bytes), url, deleted)
    }

    companion object {
        fun fromPhoto(photo: Photo, clock: Clock): PhotoModel {
            val now = clock.instant()
            return PhotoModel(id = photo.id,
                              createdAt = now,
                              updatedAt = now,
                              bytes = photo.bytes,
                              url = photo.url,
                              deleted = photo.deleted)
        }
    }
}
