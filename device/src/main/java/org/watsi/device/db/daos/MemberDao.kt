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
import org.watsi.device.db.models.DeltaModel
import org.watsi.device.db.models.MemberModel
import java.util.UUID

@Dao
abstract class MemberDao {

    @Insert
    abstract fun insert(model: MemberModel)

    @Insert(onConflict = OnConflictStrategy.ROLLBACK)
    abstract fun insertDeltas(deltas: List<DeltaModel>)

    @Insert
    abstract fun insertWithDeltas(memberModel: MemberModel, deltas: List<DeltaModel>)

    @Update
    abstract fun update(model: MemberModel)

    @Transaction
    open fun updateWithDeltas(member: MemberModel, deltas: List<DeltaModel>) {
        update(member)
        insertDeltas(deltas)
    }

    @Delete
    abstract fun destroy(model: MemberModel)

    @Query("SELECT * FROM members where id = :id LIMIT 1")
    abstract fun find(id: UUID): Flowable<MemberModel?>

    @Query("SELECT * FROM members")
    abstract fun all(): Flowable<List<MemberModel>>

    @Query("SELECT id FROM members where id = :id LIMIT 1")
    abstract fun exists(id: UUID): UUID?

    @Query("SELECT * FROM members where cardId = :cardId LIMIT 1")
    abstract fun findByCardId(cardId: String): Maybe<MemberModel>

    @Query("SELECT * FROM members where name = :name")
    abstract fun findByName(name: String): List<MemberModel>

    @Query("SELECT DISTINCT(name) FROM members")
    abstract fun uniqueNames(): List<String>

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
    abstract fun checkedInMembers(): Flowable<List<MemberModel>>

    @Query("SELECT * FROM members WHERE householdId = :householdId AND id <> :memberId")
    abstract fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): Flowable<List<MemberModel>>

    @Query("SELECT id FROM members")
    abstract fun allIds(): List<UUID>

    @Query("SELECT * FROM members WHERE photoUrl IS NOT NULL AND thumbnailPhotoId IS NULL")
    abstract fun needPhotoDownload(): Single<List<MemberModel>>

    @Query("SELECT count(*) FROM members WHERE photoUrl IS NOT NULL AND thumbnailPhotoId IS NULL")
    abstract fun needPhotoDownloadCount(): Flowable<Int>
}
