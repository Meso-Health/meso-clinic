package org.watsi.domain.relations

import org.watsi.domain.entities.Encounter
import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Photo

data class EncounterFormWithEncounterAndPhoto(val encounterForm: EncounterForm,
                                              val encounter: Encounter,
                                              val photo: Photo)
