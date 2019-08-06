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

    @Query("SELECT count(*) from members")
    fun count(): Flowable<Int>

    @Transaction
    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun findFlowableMemberWithThumbnail(id: UUID): Flowable<MemberWithThumbnailModel>

    @Query("SELECT * FROM members WHERE id = :id LIMIT 1")
    fun find(id: UUID): Maybe<MemberModel>

    @Query("SELECT * FROM members WHERE id IN (:ids)")
    fun findAll(ids: List<UUID>): Single<List<MemberModel>>

    @Query("SELECT householdId FROM members WHERE membershipNumber = :membershipNumber AND householdId IS NOT NULL ORDER BY enrolledAt DESC LIMIT 1")
    fun findHouseholdIdByMembershipNumber(membershipNumber: String): Maybe<UUID>

    @Query("SELECT householdId FROM members WHERE membershipNumber = :membershipNumber AND householdId IS NOT NULL AND archivedAt IS NULL ORDER BY enrolledAt DESC LIMIT 1")
    fun findHouseholdIdByMembershipNumberUnarchived(membershipNumber: String): Maybe<UUID>

    @Query("SELECT householdId FROM members WHERE cardId = :cardId AND householdId IS NOT NULL ORDER BY enrolledAt DESC LIMIT 1")
    fun findHouseholdIdByCardId(cardId: String): Maybe<UUID>

    @Query("SELECT householdId FROM members WHERE cardId = :cardId AND householdId IS NOT NULL AND archivedAt IS NULL ORDER BY enrolledAt DESC LIMIT 1")
    fun findHouseholdIdByCardIdUnarchived(cardId: String): Maybe<UUID>

    @Query("SELECT * FROM members WHERE householdId IS NOT NULL")
    fun all(): Flowable<List<MemberModel>>

    @Query("SELECT DISTINCT(name) FROM members")
    fun allDistinctNames(): Single<List<String>>

    @Query("SELECT * FROM members WHERE householdId IS NOT NULL AND archivedAt IS NULL")
    fun allUnarchived(): Flowable<List<MemberModel>>

    @Query("SELECT * FROM members WHERE cardId = :cardId LIMIT 1")
    fun findByCardId(cardId: String): Maybe<MemberModel>

    @Query("SELECT * FROM members WHERE cardId = :cardId AND archivedAt IS NULL LIMIT 1")
    fun findByCardIdUnarchived(cardId: String): Maybe<MemberModel>

    @Query("SELECT * FROM members WHERE id IN (:ids)")
    fun findMembersByIds(ids: List<UUID>): Single<List<MemberModel>>

    @Transaction
    @Query("SELECT * FROM members WHERE members.id IN (:ids)")
    fun findMemberRelationsByIds(ids: List<UUID>): Single<List<MemberWithIdEventAndThumbnailPhotoModel>>

    @Transaction
    @Query("SELECT * FROM members WHERE members.name IN (:names)")
    fun findMemberRelationsByNames(names: List<String>): Single<List<MemberWithIdEventAndThumbnailPhotoModel>>

    //TODO: change query to use submissionState = "started" instead of preparedAt = null once submissionState is added
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
            "WHERE (encounters.identificationEventId IS NULL OR encounters.preparedAt IS NULL)\n" +
            "ORDER BY last_identifications.occurredAt")
    fun checkedInMembers(): Flowable<List<MemberWithIdEventAndThumbnailPhotoModel>>

    //TODO: change query to use submissionState = "started" instead of preparedAt = null once submissionState is added
    @Query("SELECT (\n" +
            "   SELECT max(identification_events.occurredAt)\n" +
            "   FROM identification_events\n" +
            "   LEFT OUTER JOIN encounters ON encounters.identificationEventId = identification_events.id\n" +
            "   WHERE (encounters.identificationEventId IS NULL OR encounters.preparedAt IS NULL)\n" +
            "   AND identification_events.memberId = :memberId\n" +
            "   AND identification_events.dismissed = 0)" +
            "IS NOT NULL\n"
    )
    fun isMemberCheckedIn(memberId: UUID): Flowable<Boolean>

    @Transaction
    @Query("SELECT * FROM members WHERE householdId = :householdId")
    fun findHouseholdMembers(householdId: UUID): Flowable<List<MemberWithIdEventAndThumbnailPhotoModel>>

    @Transaction
    @Query("SELECT * FROM members WHERE householdId = :householdId AND archivedAt IS NULL")
    fun findHouseholdMembersUnarchived(householdId: UUID): Flowable<List<MemberWithIdEventAndThumbnailPhotoModel>>

    @Query("SELECT * FROM members WHERE photoUrl IS NOT NULL AND thumbnailPhotoId IS NULL AND archivedAt IS NULL")
    fun needPhotoDownload(): Single<List<MemberModel>>

    @Query("SELECT members.* FROM members\n" +
            "INNER JOIN deltas ON\n" +
            "(members.id = deltas.modelId AND\n" +
            "deltas.synced = 0 AND\n" +
            "deltas.modelName = 'MEMBER')")
    fun unsynced(): Single<List<MemberModel>>

    @Query("DELETE FROM members WHERE id IN (:ids)")
    fun delete(ids: List<UUID>)

    @Query("SELECT count(*) FROM members WHERE photoUrl IS NOT NULL AND thumbnailPhotoId IS NULL AND archivedAt IS NULL")
    fun needPhotoDownloadCount(): Flowable<Int>
}
