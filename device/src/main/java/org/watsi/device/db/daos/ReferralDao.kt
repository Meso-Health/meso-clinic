package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query
import java.util.UUID

@Dao
interface ReferralDao {

    @Query("DELETE FROM referrals")
    fun deleteAll()

    @Query("DELETE FROM referrals WHERE id = :id")
    fun delete(id: UUID)
}
