package org.watsi.domain.entities

import org.threeten.bp.LocalDate
import java.io.Serializable
import java.util.UUID

data class Referral(
    val id: UUID,
    val receivingFacility: String,
    val reason: String,
    val encounterId: UUID,
    val number: String?,
    val date: LocalDate
): Serializable
