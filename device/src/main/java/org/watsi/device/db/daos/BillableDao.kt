package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Single
import org.watsi.device.db.models.BillableModel
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

    @Query("SELECT * FROM billables")
    fun all(): Single<List<BillableModel>>

    @Query("SELECT DISTINCT(composition) FROM billables")
    fun distinctCompositions(): Single<List<String>>
}
