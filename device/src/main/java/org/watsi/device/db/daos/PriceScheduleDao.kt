package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.PriceScheduleModel
import java.util.UUID

@Dao
interface PriceScheduleDao {
    @Insert
    fun insert(model: PriceScheduleModel)

    @Insert
    fun insertWithDelta(model: PriceScheduleModel, delta: DeltaModel)

    @Query("SELECT * FROM price_schedules")
    fun all(): Single<List<PriceScheduleModel>>

    @Query("SELECT * FROM price_schedules WHERE id = :id LIMIT 1")
    fun find(id: UUID): Maybe<PriceScheduleModel>
}
