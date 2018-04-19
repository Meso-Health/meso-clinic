package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import org.watsi.device.db.models.IdentificationEventModel
import java.util.UUID

@Dao
interface IdentificationEventDao {

    @Insert
    fun insert(model: IdentificationEventModel)

    @Update
    fun update(model: IdentificationEventModel)

    @Query("SELECT identifications.id\n" +
            "FROM identifications\n" +
            "LEFT OUTER JOIN encounters ON encounters.identification_event_id = identifications.id\n" +
            "WHERE encounters.identification_event_id IS NULL\n" +
            "AND identifications.member_id = :memberId\n" +
            "AND identifications.dismissed = 0 " +
            "AND identifications.accepted = 1")
    fun openCheckIn(memberId: UUID): IdentificationEventModel?
}
