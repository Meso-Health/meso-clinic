package org.watsi.domain.entities

import java.io.Serializable
import java.util.UUID

data class Referral(
    val id: UUID,
    val receivingFacility: String,
    val reason: String,
    val encounterId: UUID,
    val number: String?
): Serializable
