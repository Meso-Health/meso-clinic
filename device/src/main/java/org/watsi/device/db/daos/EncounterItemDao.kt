package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.watsi.device.db.models.EncounterItemModel
import java.util.UUID

@Dao
interface EncounterItemDao {

    @Insert
    fun insert(model: EncounterItemModel)

    @Query("DELETE FROM encounter_items WHERE id IN (:ids)")
    fun delete(ids: List<UUID>)
}
