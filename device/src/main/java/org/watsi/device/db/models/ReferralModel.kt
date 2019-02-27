package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import java.util.UUID

@Entity(tableName = "referrals",
        foreignKeys = [
            ForeignKey(
                entity = EncounterModel::class,
                parentColumns = ["id"],
                childColumns = ["encounterId"]
            )
        ]
)
data class ReferralModel(
        @PrimaryKey val id: UUID,
        val referredToFacility: String,
        val referralReason: String,
        val encounterId: UUID,
        val referralNumber: String?
)
