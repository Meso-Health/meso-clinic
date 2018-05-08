package org.watsi.device.factories

import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.device.db.daos.DiagnosisDao
import org.watsi.device.db.models.DiagnosisModel

object DiagnosisModelFactory {

    fun build(id: Int = 1,
              description: String = "Malaria",
              searchAliases: List<String> = listOf("Mal"),
              createdAt: Instant? = null,
              updatedAt: Instant? = null,
              clock: Clock = Clock.systemUTC()) : DiagnosisModel {
        val now = Instant.now(clock)
        return DiagnosisModel(id = id,
                              description = description,
                              searchAliases = searchAliases,
                              createdAt = createdAt ?: now,
                              updatedAt = updatedAt ?: now)
    }

    fun create(diagnosisDao: DiagnosisDao,
               id: Int = 1,
               description: String = "Malaria",
               searchAliases: List<String> = listOf("Mal"),
               createdAt: Instant? = null,
               updatedAt: Instant? = null,
               clock: Clock = Clock.systemUTC()) : DiagnosisModel {
        val model = build(id = id,
                          description = description,
                          searchAliases = searchAliases,
                          createdAt = createdAt,
                          updatedAt = updatedAt,
                          clock = clock)
        diagnosisDao.insert(model)
        return model
    }
}
