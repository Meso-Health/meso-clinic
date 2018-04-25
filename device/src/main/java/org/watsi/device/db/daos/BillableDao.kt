package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import org.watsi.device.db.models.BillableModel
import org.watsi.domain.entities.Billable
import java.util.UUID

@Dao
interface BillableDao {

    @Insert
    fun insert(model: BillableModel)

    @Update
    fun update(model: BillableModel)

    @Delete
    fun delete(model: BillableModel)

    @Query("SELECT * FROM billables WHERE id = :id LIMIT 1")
    fun find(id: UUID): BillableModel?

    @Query("SELECT * FROM billables where name = :name")
    fun findByName(name: String): List<BillableModel>

    @Query("SELECT * FROM billables where type = :type")
    fun findByType(type: Billable.Type): List<BillableModel>

    @Query("SELECT DISTINCT(name) FROM billables WHERE type = 'drug'")
    fun distinctDrugNames(): List<String>

    @Query("SELECT DISTINCT(composition) FROM billables")
    fun distinctCompositions(): List<String>
}
