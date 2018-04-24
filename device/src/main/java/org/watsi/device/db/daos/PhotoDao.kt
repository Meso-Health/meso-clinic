package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import org.watsi.device.db.models.PhotoModel
import java.util.UUID

@Dao
interface PhotoDao {

    @Insert
    fun insert(model: PhotoModel)

    @Update
    fun update(model: PhotoModel)

    @Query("SELECT * FROM photos WHERE id = :id LIMIT 1")
    fun find(id: UUID): PhotoModel

    // TODO: test this query
    @Query("SELECT photos.*\n" +
            "FROM photos\n" +
            "LEFT OUTER JOIN members ON (\n" +
            "members.local_member_photo_id = photos.id OR\n" +
            "members.local_national_id_photo_id = photos.id)\n" +
            "LEFT OUTER JOIN encounter_forms ON photos.id = encounter_forms.photo_id\n" +
            "WHERE photos.deleted = 0 AND\n" +
            // or if not associated with any member encounter form model
            //  this occurs when a photo is taken but is never associated with another model
            //  (e.g. user exits the complete enrollment flow after photo but before saving)
            //  AND photo is at least 30 minutes old so we do not delete it before it
            //  has a chance to be associated with its corresponding member/encounter form
            "(members.id IS NULL AND encounter_forms.id IS NULL AND\n" +
            "photos.created_at <= datetime('now', 'localtime', '-30 Minute')))")
    fun canBeDeleted(): List<PhotoModel>
}
