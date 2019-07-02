package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Single
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.IdentificationEventModel
import java.util.UUID

@Dao
interface IdentificationEventDao {

    @Query("SELECT * FROM identification_events")
    fun all(): Flowable<List<IdentificationEventModel>>

    @Query("SELECT * FROM identification_events WHERE id = :id LIMIT 1")
    fun find(id: UUID): Single<IdentificationEventModel>

    @Insert
    fun insert(model: IdentificationEventModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(models: List<IdentificationEventModel>)

    @Query("DELETE FROM identification_events WHERE id IN (:ids)")
    fun delete(ids: List<UUID>)

    @Insert
    fun insertWithDelta(model: IdentificationEventModel, delta: DeltaModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsertWithDelta(model: IdentificationEventModel, delta: DeltaModel)

    @Query("SELECT identification_events.* FROM identification_events\n" +
            "INNER JOIN deltas ON\n" +
            "(identification_events.id = deltas.modelId AND\n" +
            "deltas.synced = 0 AND\n" +
            "deltas.modelName = 'IDENTIFICATION_EVENT')")
    fun unsynced(): Single<List<IdentificationEventModel>>

    //TODO: change query to use submissionState = "started" instead of preparedAt = null once submissionState is added
    @Query("SELECT identification_events.*\n" +
            "FROM identification_events\n" +
            "LEFT OUTER JOIN encounters ON encounters.identificationEventId = identification_events.id\n" +
            "WHERE (encounters.identificationEventId IS NULL OR encounters.preparedAt IS NULL)\n" +
            "AND identification_events.memberId = :memberId\n" +
            "AND identification_events.dismissed = 0")
    fun openCheckIn(memberId: UUID): Single<IdentificationEventModel>
}
