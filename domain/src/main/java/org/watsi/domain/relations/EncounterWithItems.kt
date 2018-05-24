package org.watsi.domain.relations

import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterItem
import java.io.Serializable

data class EncounterWithItems(val encounter: Encounter,
                              val encounterItems: List<EncounterItem>) : Serializable
