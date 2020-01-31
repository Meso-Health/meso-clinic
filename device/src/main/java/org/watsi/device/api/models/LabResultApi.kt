package org.watsi.device.api.models

import org.watsi.domain.entities.LabResult
import java.util.UUID

data class LabResultApi(
    val id: UUID,
    val result: String,
    val encounterItemId: UUID
) {
    fun toLabResult(): LabResult {
        return LabResult(
            id = id,
            result = result,
            encounterItemId = encounterItemId
        )
    }

    constructor (labResult: LabResult) : this(
        id = labResult.id,
        result = labResult.result.toLowerCase(),
        encounterItemId = labResult.encounterItemId
    )
}
