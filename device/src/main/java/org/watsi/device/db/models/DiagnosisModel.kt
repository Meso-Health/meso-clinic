package org.watsi.device.db.models

import android.arch.persistence.room.Entity
import org.threeten.bp.Clock
import org.threeten.bp.Instant
import org.watsi.domain.entities.Diagnosis
import java.util.UUID

@Entity(tableName = "diagnoses")
data class DiagnosisModel(val id: UUID,
                          val createdAt: Instant,
                          val updatedAt: Instant,
                          val description: String,
                          val searchAliases: List<String>) {

    fun toDiagnosis(): Diagnosis {
        return Diagnosis(id = id, description = description, searchAliases = searchAliases)
    }

    companion object {
        fun fromDiagnosis(diagnosis: Diagnosis, clock: Clock): DiagnosisModel {
            val now = clock.instant()
            return DiagnosisModel(id = diagnosis.id,
                                  createdAt = now,
                                  updatedAt = now,
                                  description = diagnosis.description,
                                  searchAliases = diagnosis.searchAliases)
        }
    }
}
