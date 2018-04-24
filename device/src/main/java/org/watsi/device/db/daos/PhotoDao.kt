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

    // TODO: fix this query
    @Query("SELECT * FROM photos")
    fun canBeDeleted(): List<PhotoModel>
}
