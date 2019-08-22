package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.models.DiagnosisModel

object DiagnosisModelFactory {

    fun build(id: Int = 0,
              description: String = "Malaria",
              searchAliases: List<String> = listOf("Mal"),
              createdAt: Instant? = null,
              updatedAt: Instant? = null,
              active: Boolean = true,
              clock: Clock = Clock.systemUTC()) : DiagnosisModel {
        val now = Instant.now(clock)
        return DiagnosisModel(
            id = id,
            description = description,
            searchAliases = searchAliases,
            createdAt = createdAt ?: now,
            updatedAt = updatedAt ?: now,
            active = active
        )
    }

    fun create(diagnosisDao: DiagnosisDao,
               id: Int = 0,
               description: String = "Malaria",
               searchAliases: List<String> = listOf("Mal"),
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               active: Boolean = true,
               clock: Clock = Clock.systemUTC()) : DiagnosisModel {
        val model = build(
            id = id,
            description = description,
            searchAliases = searchAliases,
            createdAt = createdAt,
            updatedAt = updatedAt,
            clock = clock,
            active = active
        )
        diagnosisDao.insert(model)
        return model
    }
}
