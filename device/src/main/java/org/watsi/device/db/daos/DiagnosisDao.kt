package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import org.watsi.device.db.models.DiagnosisModel

@Dao
interface DiagnosisDao {

    @Insert
    fun insert(model: DiagnosisModel)

    @Delete
    fun delete(model: DiagnosisModel)

    @Query("SELECT * FROM diagnoses WHERE searchAliases LIKE :query")
    fun searchAliasLike(query: String): List<DiagnosisModel>

    @Query("SELECT DISTINCT(description) FROM diagnoses")
    fun uniqueDescriptions(): Set<String>

    @Query("SELECT * FROM diagnoses WHERE description = :description")
    fun findByDescription(description: String): List<DiagnosisModel>
}
