package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.ForeignKey
import android.arch.persistence.room.Index
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.IdentificationEvent
import java.util.UUID

@Entity(
    tableName = "identification_events",
    indices = [
        Index("memberId"),
        Index("throughMemberId"),
        Index("occurredAt")
    ],
    foreignKeys = [
        ForeignKey(
            entity = MemberModel::class,
            parentColumns = ["id"],
            childColumns = ["memberId"]
        ),
        ForeignKey(
            entity = MemberModel::class,
            parentColumns = ["id"],
            childColumns = ["throughMemberId"]
        )
    ]
)
data class IdentificationEventModel(@PrimaryKey val id: UUID,
                                    val createdAt: Instant,
                                    val updatedAt: Instant,
                                    val memberId: UUID,
                                    val throughMemberId: UUID?,
                                    val occurredAt: Instant,
                                    val searchMethod: IdentificationEvent.SearchMethod,
                                    val clinicNumber: Int?,
                                    val clinicNumberType: IdentificationEvent.ClinicNumberType?,
                                    val dismissed: Boolean) {

    fun toIdentificationEvent(): IdentificationEvent {
        return IdentificationEvent(id = id,
                                   memberId = memberId,
                                   throughMemberId = throughMemberId,
                                   occurredAt = occurredAt,
                                   searchMethod = searchMethod,
                                   clinicNumber = clinicNumber,
                                   clinicNumberType = clinicNumberType,
                                   dismissed = dismissed)
    }

    companion object {
        fun fromIdentificationEvent(idEvent: IdentificationEvent,
                                    clock: Clock): IdentificationEventModel {
            val now = clock.instant()
            return IdentificationEventModel(id = idEvent.id,
                                            createdAt = now,
                                            updatedAt = now,
                                            memberId = idEvent.memberId,
                                            throughMemberId = idEvent.throughMemberId,
                                            occurredAt = idEvent.occurredAt,
                                            searchMethod = idEvent.searchMethod,
                                            clinicNumber = idEvent.clinicNumber,
                                            clinicNumberType = idEvent.clinicNumberType,
                                            dismissed = idEvent.dismissed
            )
        }
    }
}
