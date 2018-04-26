package org.watsi.domain.entities

import java.io.Serializable
import java.util.UUID

data class EncounterItem(val id: UUID,
                         val encounterId: UUID,
                         val billableId: UUID,
                         var quantity: Int) : Serializable
