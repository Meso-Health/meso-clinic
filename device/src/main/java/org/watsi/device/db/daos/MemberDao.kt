package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Flowable
import io.reactivex.Maybe
import org.watsi.device.db.models.MemberModel
import java.util.UUID

@Dao
interface MemberDao {

    @Insert
    fun insert(model: MemberModel)

    @Update
    fun update(model: MemberModel)

    @Delete
    fun destroy(model: MemberModel)

    @Query("SELECT * FROM members where id = :id LIMIT 1")
    fun find(id: UUID): Flowable<MemberModel?>

    @Query("SELECT * FROM members")
    fun all(): Flowable<List<MemberModel>>

    @Query("SELECT id FROM members where id = :id LIMIT 1")
    fun exists(id: UUID): UUID?

    @Query("SELECT * FROM members where cardId = :cardId LIMIT 1")
    fun findByCardId(cardId: String): Maybe<MemberModel>

    @Query("SELECT * FROM members where name = :name")
    fun findByName(name: String): List<MemberModel>

    @Query("SELECT DISTINCT(name) FROM members")
    fun uniqueNames(): List<String>

    @Query("SELECT members.*\n" +
            "FROM members\n" +
            "INNER JOIN (\n" +
            "   SELECT id, memberId, max(occurredAt) AS occurredAt\n" +
            "   FROM identification_events\n" +
            "   WHERE accepted = 1\n" +
            "   AND dismissed = 0\n" +
            "   GROUP BY memberId\n" +
            ") last_identifications on last_identifications.memberId = members.id\n" +
            "LEFT OUTER JOIN encounters ON encounters.identificationEventId = last_identifications.id\n" +
            "WHERE encounters.identificationEventId IS NULL\n" +
            "ORDER BY last_identifications.occurredAt")
    fun checkedInMembers(): Flowable<List<MemberModel>>

    @Query("SELECT * FROM members WHERE householdId = :householdId AND id <> :memberId")
    fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): Flowable<List<MemberModel>>

    @Query("SELECT id FROM members")
    fun allIds(): List<UUID>
}
