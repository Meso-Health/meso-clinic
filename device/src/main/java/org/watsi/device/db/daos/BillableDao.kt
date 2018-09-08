package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.BillableWithPriceScheduleListModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.PriceScheduleModel
import org.watsi.domain.entities.Billable
import java.util.UUID

@Dao
interface BillableDao {

    @Insert
    fun insert(model: BillableModel)

    @Insert
    fun insertWithDelta(model: BillableModel, delta: DeltaModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(billableModels: List<BillableModel>, priceScheduleModels: List<PriceScheduleModel>)

    @Delete
    fun delete(model: BillableModel)

    @Query("SELECT * FROM billables")
    fun all(): Single<List<BillableModel>>

    @Query("SELECT * FROM billables")
    fun allWithPrice(): Single<List<BillableWithPriceScheduleListModel>>

    @Query("SELECT * FROM billables WHERE type = :type")
    fun ofType(type: Billable.Type): Single<List<BillableWithPriceScheduleListModel>>

    @Query("SELECT * FROM billables WHERE id = :id LIMIT 1")
    fun find(id: UUID): Maybe<BillableModel>

    @Query("SELECT DISTINCT(composition) FROM billables WHERE composition IS NOT NULL")
    fun distinctCompositions(): Single<List<String>>

    @Query("DELETE FROM billables WHERE id IN (:ids)")
    fun delete(ids: List<UUID>)

    @Query("SELECT billables.* FROM billables\n" +
            "INNER JOIN deltas ON\n" +
                "(billables.id = deltas.modelId AND\n" +
                "deltas.synced = 0 AND\n" +
                "deltas.modelName = 'BILLABLE')")
    fun unsynced(): Single<List<BillableModel>>

    @Query("SELECT * FROM billables WHERE name IN ('Consultation', 'Medical Form')")
    fun opdDefaults(): Single<List<BillableWithPriceScheduleListModel>>
}
