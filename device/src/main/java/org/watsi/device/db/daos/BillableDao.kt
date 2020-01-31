package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.BillableWithPriceSchedulesModel
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

    @Query("SELECT count(*) from billables WHERE active = 1")
    fun countActive(): Single<Int>

    @Query("SELECT * FROM billables WHERE active = 1")
    fun allActive(): Single<List<BillableModel>>

    @Transaction
    @Query("SELECT * FROM billables WHERE active = 1")
    fun allActiveWithPrice(): Single<List<BillableWithPriceSchedulesModel>>

    @Transaction
    @Query("SELECT id FROM billables WHERE type = :type AND active = 1")
    fun allActiveIdsOfType(type: Billable.Type): Single<List<UUID>>

    @Query("SELECT DISTINCT(type) FROM billables WHERE active = 1 ORDER BY type")
    fun uniqueTypes(): Flowable<List<Billable.Type>>

    @Query("SELECT * FROM billables WHERE id = :id LIMIT 1")
    fun find(id: UUID): Maybe<BillableModel>

    @Query("SELECT * FROM billables WHERE id IN (:ids)")
    fun find(ids: List<UUID>): Single<List<BillableModel>>

    @Transaction
    @Query("SELECT * FROM billables WHERE id IN (:ids)")
    fun findWithPrice(ids: List<UUID>): Single<List<BillableWithPriceSchedulesModel>>

    @Query("SELECT billables.* FROM billables\n" +
            "INNER JOIN deltas ON\n" +
                "(billables.id = deltas.modelId AND\n" +
                "deltas.synced = 0 AND\n" +
                "deltas.modelName = 'BILLABLE')")
    fun unsynced(): Single<List<BillableModel>>

    @Transaction
    @Query("SELECT * FROM billables WHERE name IN ('Consultation', 'Medical Form')")
    fun opdDefaults(): Single<List<BillableWithPriceSchedulesModel>>
}
