package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import android.arch.persistence.room.Update
import io.reactivex.Single
import org.watsi.device.db.models.MemberWithRawPhotoModel
import org.watsi.device.db.models.PhotoModel
import java.util.UUID

@Dao
interface PhotoDao {

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    fun find(id: UUID): Single<PhotoModel>

    @Transaction
    @Query("SELECT * FROM members WHERE id = :memberId LIMIT 1")
    fun findMemberWithRawPhoto(memberId: UUID): Single<MemberWithRawPhotoModel>

    @Insert
    fun insert(model: PhotoModel)

    @Update
    fun update(model: PhotoModel)

    @Delete
    fun destroy(model: PhotoModel)

    // strftime('%s', 'now') returns the current UNIX time, so we multiply by 1000
    // to convert to milliseconds since epoch which is how our Instant types are stored
    // also include a 30 minute window before considering a photo safe to delete to handle
    // scenario where model is created, but not yet assigned to a member / encounter form
    // due to user being in the process of entering data
    @Query("SELECT photos.*\n" +
            "FROM photos\n" +
            "LEFT OUTER JOIN members ON\n" +
                "(photos.id = members.thumbnailPhotoId OR photos.id = members.photoId)\n" +
            "LEFT OUTER JOIN encounter_forms ON photos.id = encounter_forms.photoId\n" +
            "WHERE (encounter_forms.id IS NULL AND members.id IS NULL AND\n" +
                "photos.createdAt <= (strftime('%s', 'now', '-30 Minute') * 1000))")
    fun canBeDeleted(): Single<List<PhotoModel>>
}
