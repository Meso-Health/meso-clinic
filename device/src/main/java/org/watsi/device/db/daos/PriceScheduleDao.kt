package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import io.reactivex.Single
import org.watsi.device.db.models.PriceScheduleModel

@Dao
interface PriceScheduleDao {
    @Insert
    fun insert(model: PriceScheduleModel)

    @Query("SELECT * FROM price_schedules")
    fun all(): Single<List<PriceScheduleModel>>
}
