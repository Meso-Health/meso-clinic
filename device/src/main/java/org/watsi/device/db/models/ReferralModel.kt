package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.LocalDate
import org.watsi.domain.entities.Referral
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
    val receivingFacility: String,
    val reason: String,
    val encounterId: UUID,
    val number: String?,
    val date: LocalDate
) {
    fun toReferral(): Referral {
        return Referral(
            id = id,
            receivingFacility = receivingFacility,
            reason = reason,
            encounterId = encounterId,
            number = number,
            date = date
        )
    }

    companion object {
        fun fromReferral(referral: Referral): ReferralModel {
            return ReferralModel(
                id = referral.id,
                receivingFacility = referral.receivingFacility,
                reason = referral.reason,
                encounterId = referral.encounterId,
                number = referral.number,
                date = referral.date
            )
        }
    }
}
