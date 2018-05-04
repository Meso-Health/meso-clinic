package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.EncounterFormDao
import org.watsi.device.db.models.EncounterFormModel
import java.util.UUID

object EncounterFormModelFactory {

    fun build(id: UUID = UUID.randomUUID(),
              encounterId: UUID = UUID.randomUUID(),
              photoId: UUID = UUID.randomUUID(),
              createdAt: Instant? = null,
              updatedAt: Instant? = null,
              clock: Clock = Clock.systemUTC()) : EncounterFormModel {
        val now = Instant.now(clock)
        return EncounterFormModel(id = id,
                                  encounterId = encounterId,
                                  photoId = photoId,
                                  createdAt = createdAt ?: now,
                                  updatedAt = updatedAt ?: now)
    }

    fun create(encounterFormDao: EncounterFormDao,
               id: UUID = UUID.randomUUID(),
               encounterId: UUID = UUID.randomUUID(),
               photoId: UUID = UUID.randomUUID(),
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               clock: Clock = Clock.systemUTC()) : EncounterFormModel {
        val model = build(id = id,
                          encounterId = encounterId,
                          photoId = photoId,
                          createdAt = createdAt,
                          updatedAt = updatedAt,
                          clock = clock)
        encounterFormDao.insert(model)
        return model
    }
}
