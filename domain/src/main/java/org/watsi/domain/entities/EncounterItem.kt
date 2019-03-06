package org.watsi.domain.entities

import java.io.Serializable
import java.util.UUID

data class EncounterItem(
    val id: UUID,
    val encounterId: UUID,
    val quantity: Int,
    val priceScheduleId: UUID,
    val priceScheduleIssued: Boolean,
    val stockout: Boolean = false
) : Serializable
