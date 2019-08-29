package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import android.arch.persistence.room.Update
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.device.db.models.BillableModel
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.DiagnosisModel
import org.watsi.device.db.models.EncounterFormModel
import org.watsi.device.db.models.EncounterItemModel
import org.watsi.device.db.models.EncounterModel
import org.watsi.device.db.models.EncounterWithExtrasModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.PriceScheduleModel
import org.watsi.device.db.models.ReferralModel
import java.util.UUID

@Dao
interface EncounterDao {
    @Transaction
    @Query("SELECT * FROM encounters WHERE id = :id LIMIT 1")
    fun find(id: UUID): Single<EncounterWithExtrasModel>

    @Transaction
    @Query("SELECT * FROM encounters WHERE id IN (:ids)")
    fun findAll(ids: List<UUID>): Single<List<EncounterModel>>

    @Transaction
    @Query("SELECT * FROM encounters WHERE id IN (:ids)")
    fun findAllWithExtras(ids: List<UUID>): Single<List<EncounterWithExtrasModel>>

    @Update
    fun update(encounters: List<EncounterModel>): Int

    @Insert
    fun insert(encounterModel: EncounterModel,
               encounterItemModels: List<EncounterItemModel>,
               encounterFormModels: List<EncounterFormModel>,
               referralModels: List<ReferralModel>,
               deltaModels: List<DeltaModel>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(
        memberModels: List<MemberModel>,
        encounterModels: List<EncounterModel>,
        billableModels: List<BillableModel>,
        priceScheduleModels: List<PriceScheduleModel>,
        encounterItemModels: List<EncounterItemModel>,
        referralModels: List<ReferralModel>,
        diagnosisModels: List<DiagnosisModel>
    )

    @Delete
    fun delete(
        referralModels: List<ReferralModel>,
        encounterItemModels: List<EncounterItemModel>,
        encounterModel: EncounterModel
    )

    @Transaction
    @Query("SELECT * from encounters WHERE submittedAt IS NULL ORDER BY occurredAt")
    fun pending(): Flowable<List<EncounterWithExtrasModel>>

    @Transaction
    @Query("SELECT * from encounters WHERE submittedAt IS NULL ORDER BY occurredAt LIMIT 1")
    fun loadOnePendingClaim(): Maybe<EncounterWithExtrasModel>

    @Transaction
    @Query("SELECT COUNT(*) from encounters WHERE submittedAt IS NULL")
    fun pendingCount(): Flowable<Int>

    @Transaction
    @Query("SELECT * from encounters WHERE adjudicationState = 'RETURNED' ORDER BY occurredAt")
    fun returned(): Flowable<List<EncounterWithExtrasModel>>

    @Transaction
    @Query("SELECT * from encounters WHERE adjudicationState = 'RETURNED' ORDER BY occurredAt LIMIT 1")
    fun loadOneReturnedClaim(): Maybe<EncounterWithExtrasModel>

    @Transaction
    @Query("SELECT COUNT(*) from encounters WHERE adjudicationState = 'RETURNED'")
    fun returnedCount(): Flowable<Int>

    @Transaction
    @Query("SELECT id from encounters WHERE adjudicationState = 'RETURNED' ORDER BY occurredAt")
    fun returnedIds(): Single<List<UUID>>

    @Transaction
    @Query("SELECT DISTINCT(revisedEncounterId) from encounters WHERE revisedEncounterId IS NOT NULL ORDER BY occurredAt")
    fun revisedIds(): Single<List<UUID>>

    @Query("SELECT encounters.* FROM encounters\n" +
            "INNER JOIN deltas ON\n" +
            "(encounters.id = deltas.modelId AND\n" +
            "deltas.synced = 0 AND\n" +
            "deltas.modelName = 'ENCOUNTER')")
    fun unsynced(): Single<List<EncounterWithExtrasModel>>
}
