package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Insert
import android.arch.persistence.room.Query
import android.arch.persistence.room.Update
import io.reactivex.Single
import org.watsi.device.db.models.IdentificationEventModel
import java.util.UUID

@Dao
interface IdentificationEventDao {

    @Insert
    fun insert(model: IdentificationEventModel)

    @Update
    fun update(model: IdentificationEventModel)

    @Query("SELECT identification_events.*\n" +
            "FROM identification_events\n" +
            "LEFT OUTER JOIN encounters ON encounters.identificationEventId = identification_events.id\n" +
            "WHERE encounters.identificationEventId IS NULL\n" +
            "AND identification_events.memberId = :memberId\n" +
            "AND identification_events.dismissed = 0 " +
            "AND identification_events.accepted = 1")
    fun openCheckIn(memberId: UUID): Single<IdentificationEventModel?>
}
