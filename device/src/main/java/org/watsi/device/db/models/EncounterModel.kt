package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import android.arch.persistence.room.PrimaryKey
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Encounter
import java.util.UUID

@Entity(tableName = "encounters")
data class EncounterModel(@PrimaryKey val id: UUID,
                          val createdAt: Instant,
                          val updatedAt: Instant,
                          val memberId: UUID,
                          val identificationEventId: UUID,
                          val occurredAt: Instant,
                          val backdatedOccurredAt: Boolean,
                          val copaymentPaid: Boolean,
                          val diagnoses: List<Int>) {

    fun toEncounter(): Encounter {
        return Encounter(id = id,
                         memberId = memberId,
                         identificationEventId = identificationEventId,
                         occurredAt = occurredAt,
                         backdatedOccurredAt = backdatedOccurredAt,
                         copaymentPaid = copaymentPaid,
                         diagnoses = diagnoses)
    }

    companion object {
        fun fromEncounter(encounter: Encounter, clock: Clock): EncounterModel {
            val now = clock.instant()
            return EncounterModel(id = encounter.id,
                                  createdAt = now,
                                  updatedAt = now,
                                  memberId = encounter.memberId,
                                  identificationEventId = encounter.identificationEventId,
                                  occurredAt = encounter.occurredAt,
                                  backdatedOccurredAt = encounter.backdatedOccurredAt,
                                  copaymentPaid = encounter.copaymentPaid,
                                  diagnoses = encounter.diagnoses)
        }
    }
}
