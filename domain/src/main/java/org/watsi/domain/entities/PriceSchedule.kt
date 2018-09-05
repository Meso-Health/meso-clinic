package org.watsi.domain.entities

import org.threeten.bp.Instant
import java.util.UUID

data class PriceSchedule(
    val id: UUID,
    val issuedAt: Instant,
    val billableId: UUID,
    val price: Int,
    val previousPriceScheduleModelId: UUID?
)
