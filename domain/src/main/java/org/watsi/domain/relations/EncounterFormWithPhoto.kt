package org.watsi.domain.relations

import org.watsi.domain.entities.EncounterForm
import org.watsi.domain.entities.Photo

data class EncounterFormWithPhoto(val encounterForm: EncounterForm, val photo: Photo)
