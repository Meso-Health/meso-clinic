package org.watsi.device.db.daos

import android.arch.persistence.room.Dao
import android.arch.persistence.room.Query

@Dao
interface ReferralDao {

    @Query("DELETE FROM referrals")
    fun deleteAll()
}
