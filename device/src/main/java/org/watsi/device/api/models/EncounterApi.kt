package org.watsi.device.api.models

import com.google.gson.Gson
import com.google.gson.JsonArray
import org.threeten.bp.Instant
import org.watsi.domain.relations.EncounterWithItems
import java.util.UUID

data class EncounterApi(
    val id: UUID,
    val memberId: UUID,
    val identificationEventId: UUID?,
    val occurredAt: Instant,
    val preparedAt: Instant?,
    val backdatedOccurredAt: Boolean,
    val copaymentPaid: Boolean? = false,
    val diagnosisIds: JsonArray,
    val encounterItems: List<EncounterItemApi>,
    val visitType: String?,
    val revisedEncounterId: UUID?,
    val providerComment: String?,
    val claimId: String,
    val submittedAt: Instant?
) {

    constructor (encounterWithItems: EncounterWithItems) : this(
        id = encounterWithItems.encounter.id,
        memberId = encounterWithItems.encounter.memberId,
        identificationEventId = encounterWithItems.encounter.identificationEventId,
        occurredAt = encounterWithItems.encounter.occurredAt,
        preparedAt = encounterWithItems.encounter.preparedAt,
        backdatedOccurredAt = encounterWithItems.encounter.backdatedOccurredAt,
        copaymentPaid = encounterWithItems.encounter.copaymentPaid,
        diagnosisIds = Gson().fromJson(encounterWithItems.encounter.diagnoses.toString(), JsonArray::class.java),
        encounterItems = encounterWithItems.encounterItems.map { EncounterItemApi(it) },
        visitType = encounterWithItems.encounter.visitType,
        revisedEncounterId = encounterWithItems.encounter.revisedEncounterId,
        providerComment = encounterWithItems.encounter.providerComment,
        claimId = encounterWithItems.encounter.claimId,
        submittedAt = encounterWithItems.encounter.submittedAt
    )
}
