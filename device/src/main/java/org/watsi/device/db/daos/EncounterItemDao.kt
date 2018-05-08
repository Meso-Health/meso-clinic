package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import org.watsi.device.db.models.EncounterItemModel

@Dao
interface EncounterItemDao {

    @Insert
    fun insert(model: EncounterItemModel)
}
