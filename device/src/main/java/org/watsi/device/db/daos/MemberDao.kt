package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.OnConflictStrategy
import android.arch.persistence.room.Query
import android.arch.persistence.room.Transaction
import io.reactivex.Flowable
import io.reactivex.Maybe
import io.reactivex.Single
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.MemberModel
import org.watsi.device.db.models.MemberWithIdEventAndThumbnailPhotoModel
import org.watsi.device.db.relations.MemberWithThumbnailModel
import java.util.UUID

@Dao
interface MemberDao {

    @Insert
    fun insert(model: MemberModel)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(model: MemberModel, deltas: List<DeltaModel> = emptyList())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun upsert(models: List<MemberModel>)

    @Transaction
    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun findFlowableMemberWithThumbnail(id: UUID): Flowable<MemberWithThumbnailModel>

    @Query("SELECT * FROM members where id = :id LIMIT 1")
    fun findFlowable(id: UUID): Flowable<MemberModel>

    @Query("SELECT * FROM members where id = :id LIMIT 1")
    fun find(id: UUID): Single<MemberModel>

    @Query("SELECT * FROM members")
    fun all(): Flowable<List<MemberModel>>

    @Query("SELECT * FROM members where cardId = :cardId LIMIT 1")
    fun findByCardId(cardId: String): Maybe<MemberModel>

    @Transaction
    @Query("SELECT * FROM members WHERE members.id IN (:ids)")
    fun byIds(ids: List<UUID>): Single<List<MemberWithIdEventAndThumbnailPhotoModel>>

    @Transaction
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
    fun checkedInMembers(): Flowable<List<MemberWithIdEventAndThumbnailPhotoModel>>

    @Query("SELECT EXISTS(\n" +
            "   SELECT *\n" +
            "   FROM (\n" +
            "       SELECT id, memberId, max(occurredAt) AS occurredAt\n" +
            "       FROM identification_events\n" +
            "       WHERE dismissed = 0\n" +
            "       AND memberId = :memberId\n" +
            "   ) last_identifications\n" +
            "   LEFT OUTER JOIN encounters ON encounters.identificationEventId = last_identifications.id\n" +
            "   WHERE encounters.identificationEventId IS NULL\n" +
            "   AND last_identifications.memberId = :memberId)")
    fun isMemberCheckedIn(memberId: UUID): Flowable<Boolean>

    @Transaction
    @Query("SELECT * FROM members WHERE householdId = :householdId AND id != :memberId")
    fun remainingHouseholdMembers(memberId: UUID, householdId: UUID): Flowable<List<MemberWithIdEventAndThumbnailPhotoModel>>

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
