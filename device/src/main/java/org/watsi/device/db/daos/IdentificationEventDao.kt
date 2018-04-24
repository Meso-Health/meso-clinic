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

    @Query("SELECT identification_events.id\n" +
            "FROM identification_events\n" +
            "LEFT OUTER JOIN encounters ON encounters.identification_event_id = identification_events.id\n" +
            "WHERE encounters.identification_event_id IS NULL\n" +
            "AND identification_events.member_id = :memberId\n" +
            "AND identification_events.dismissed = 0 " +
            "AND identification_events.accepted = 1")
    fun openCheckIn(memberId: UUID): IdentificationEventModel?
}
