package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.MemberModel
import java.util.UUID

@Dao
interface MemberDao {

    @Insert
    fun insert(model: MemberModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(model: MemberModel, deltas: List<DeltaModel> = emptyList())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(models: List<MemberModel>)

    @Query("SELECT * FROM members where id = :id LIMIT 1")
    fun find(id: UUID): Flowable<MemberModel?>

    @Query("SELECT * FROM members")
    fun all(): Flowable<List<MemberModel>>

    @Query("SELECT * FROM members")
    fun allSingle(): Single<List<MemberModel>>

    @Query("SELECT * FROM members where cardId = :cardId LIMIT 1")
    fun findByCardId(cardId: String): Maybe<MemberModel>

    @Query("SELECT members.*\n" +
            "FROM members\n" +
            "INNER JOIN (\n" +
            "   SELECT id, memberId, max(occurredAt) AS occurredAt\n" +
            "   FROM identification_events\n" +
            "   WHERE dismissed = 0\n" +
            "   GROUP BY memberId\n" +
            ") last_identifications on last_identifications.memberId = members.id\n" +
            "LEFT OUTER JOIN encounters ON encounters.identificationEventId = last_identifications.id\n" +
            "WHERE encounters.identificationEventId IS NULL\n" +
            "ORDER BY last_identifications.occurredAt")
    fun checkedInMembers(): Flowable<List<MemberModel>>

    @Query("SELECT * FROM members WHERE householdId = :householdId AND id <> :memberId")
    fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): Flowable<List<MemberModel>>

    @Query("SELECT * FROM members WHERE photoUrl IS NOT NULL AND thumbnailPhotoId IS NULL")
    fun needPhotoDownload(): Single<List<MemberModel>>

    @Query("SELECT members.* FROM members\n" +
            "INNER JOIN deltas ON\n" +
            "(members.id = deltas.modelId AND\n" +
            "deltas.synced = 0 AND\n" +
            "deltas.modelName = 'MEMBER')")
    fun unsynced(): Single<List<MemberModel>>

    @Query("DELETE FROM members WHERE id NOT IN (:ids)")
    fun deleteNotInList(ids: List<UUID>)

    @Query("SELECT count(*) FROM members WHERE photoUrl IS NOT NULL AND thumbnailPhotoId IS NULL")
    fun needPhotoDownloadCount(): Flowable<Int>
}
