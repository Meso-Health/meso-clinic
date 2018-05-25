package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.EncounterDao
import org.watsi.device.db.models.EncounterModel
import java.util.UUID

object EncounterModelFactory {

    fun build(id: UUID = UUID.randomUUID(),
              memberId: UUID = UUID.randomUUID(),
              identificationEventId: UUID = UUID.randomUUID(),
              occurredAt: Instant = Instant.now(),
              backdatedOccurredAt: Boolean = false,
              copaymentPaid: Boolean = true,
              diagnoses: List<Int> = emptyList(),
              createdAt: Instant? = null,
              updatedAt: Instant? = null,
              clock: Clock = Clock.systemUTC()) : EncounterModel {
        val now = Instant.now(clock)
        return EncounterModel(id = id,
                              memberId = memberId,
                              identificationEventId = identificationEventId,
                              occurredAt = occurredAt,
                              backdatedOccurredAt = backdatedOccurredAt,
                              copaymentPaid = copaymentPaid,
                              diagnoses = diagnoses,
                              createdAt = createdAt ?: now,
                              updatedAt = updatedAt ?: now)
    }

    fun create(encounterDao: EncounterDao,
               id: UUID = UUID.randomUUID(),
               memberId: UUID = UUID.randomUUID(),
               identificationEventId: UUID = UUID.randomUUID(),
               occurredAt: Instant = Instant.now(),
               backdatedOccurredAt: Boolean = false,
               copaymentPaid: Boolean = true,
               diagnoses: List<Int> = emptyList(),
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               clock: Clock = Clock.systemUTC()) : EncounterModel {
        val model = build(id = id,
                          memberId = memberId,
                          identificationEventId = identificationEventId,
                          occurredAt = occurredAt,
                          backdatedOccurredAt = backdatedOccurredAt,
                          copaymentPaid = copaymentPaid,
                          diagnoses = diagnoses,
                          createdAt = createdAt,
                          updatedAt = updatedAt,
                          clock = clock)
        encounterDao.insert(model, emptyList(), emptyList(), emptyList(), emptyList())
        return model
    }
}
