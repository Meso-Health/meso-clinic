package org.watsi.device.api.models

import com.google.gson.annotations.SerializedName
import org.watsi.domain.entities.Diagnosis

data class DiagnosisApi(val id: Int,
                        val description: String,
                        @SerializedName("search_aliases") val searchAliases: List<String>) {

    fun toDiagnosis(): Diagnosis {
        return Diagnosis(id, description, searchAliases)
    }
}
