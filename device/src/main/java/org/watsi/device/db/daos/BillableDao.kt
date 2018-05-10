package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Single
import org.watsi.device.db.models.BillableModel
import java.util.UUID

@Dao
interface BillableDao {

    @Insert
    fun insert(model: BillableModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(models: List<BillableModel>)

    @Delete
    fun delete(model: BillableModel)

    @Query("SELECT * FROM billables")
    fun all(): Single<List<BillableModel>>

    @Query("SELECT DISTINCT(composition) FROM billables WHERE composition IS NOT NULL")
    fun distinctCompositions(): Single<List<String>>

    @Query("DELETE FROM billables WHERE id NOT IN (:ids)")
    fun deleteNotInList(ids: List<UUID>)

    @Query("SELECT billables.* FROM billables\n" +
            "INNER JOIN deltas ON\n" +
                "(billables.id = deltas.modelId AND\n" +
                "deltas.synced = 0 AND\n" +
                "deltas.modelName = 'BILLABLE')")
    fun unsynced(): Single<List<BillableModel>>
}
