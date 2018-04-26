package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import org.watsi.device.db.models.DiagnosisModel

@Dao
interface DiagnosisDao {

    @Insert
    fun insert(model: DiagnosisModel)

    @Update
    fun update(model: DiagnosisModel)

    @Delete
    fun delete(model: DiagnosisModel)

    @Query("SELECT * FROM diagnoses WHERE id = :id LIMIT 1")
    fun find(id: Int): DiagnosisModel?

    @Query("SELECT * FROM diagnoses")
    fun all(): List<DiagnosisModel>
}
