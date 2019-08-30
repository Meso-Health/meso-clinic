package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Single
import org.threeten.bp.LocalDate
import org.watsi.device.db.models.EnrollmentPeriodModel

@Dao
interface EnrollmentPeriodDao {

    @Insert
    fun insert(model: EnrollmentPeriodModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(models: List<EnrollmentPeriodModel>)

    @Query("SELECT * FROM enrollment_periods WHERE :date BETWEEN startDate AND endDate")
    fun active(date: LocalDate): Single<List<EnrollmentPeriodModel>>
}
