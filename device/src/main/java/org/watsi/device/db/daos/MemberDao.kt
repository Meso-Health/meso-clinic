package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Delete
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
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
    fun find(id: UUID): MemberModel

    @Query("SELECT * FROM members where cardId = :cardId LIMIT 1")
    fun findByCardId(cardId: String): MemberModel?

    @Query("SELECT * FROM members where name = :name")
    fun findByName(name: String): List<MemberModel>

    @Query("SELECT * FROM members WHERE cardId LIKE :cardId")
    fun cardIdLike(query: String): List<MemberModel>

    @Query("SELECT DISTINCT(name) FROM members")
    fun uniqueNames(): Set<String>

    @Query("SELECT members.*\n" +
            "FROM members\n" +
            "INNER JOIN (\n" +
            "   SELECT id, member_id, max(occurred_at) AS occurred_at\n" +
            "   FROM identifications\n" +
            "   WHERE accepted = 1\n" +
            "   AND dismissed = 0\n" +
            "   GROUP BY member_id\n" +
            ") last_identifications on last_identifications.member_id = members.id\n" +
            "LEFT OUTER JOIN encounters ON encounters.identification_event_id = last_identifications.id\n" +
            "WHERE encounters.identification_event_id IS NULL\n" +
            "ORDER BY last_identifications.occurred_at")
    fun checkedInMembers(): List<MemberModel>

    @Query("SELECT * FROM members WHERE householdId = :householdId AND id <> :memberId")
    fun remainingHouseholdMembers(householdId: UUID, memberId: UUID): List<MemberModel>

    @Query("SELECT id FROM members")
    fun allIds(): Set<UUID>
}
